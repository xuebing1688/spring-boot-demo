#include <cassert>
#include <cctype>
#include <cstdarg>
#include <cstdlib>
#include <cstring>
#include <fstream>

#include <stdio.h>
#include <string>
#include <sys/stat.h>
#include <vector>

static inline std::string &AppendFormat(std::string &input, const char *fmt,
                                        ...) {
  if (fmt == nullptr || *fmt == '\0')
    return input;

  std::string text;
  int size = 1024;
  va_list ap;
  while (1) {
    text.resize(size);
    va_start(ap, fmt);
    int n = vsnprintf((char *)text.c_str(), size, fmt, ap);
    va_end(ap);

    if (n > -1 && n < size) {
      text.resize(n);
      break;
    }
    size = (n > -1) ? n + 1 : size * 2;
    if (size < 0) {
      assert(false);
      break;
    }
  }
  input.append(text);
  return input;
}

#pragma pack(1)
struct KafkaTransBinaryHead {
  uint8_t magic;
  uint8_t headerLen;
  uint8_t ipVersion : 4;
  uint8_t reserve : 2;
  uint8_t isFirst : 1;
  uint8_t isDone : 1;
  uint8_t netlinkId;
  uint32_t dateLen;
  uint16_t sequence;
  uint16_t pktCount;
  uint8_t ipaddr
      [0]; // 要保证内存拷贝时能获取值，目前处理方式，放在协议最后一个，保证内存地址连续
};
#pragma pack()

static void PrintData(const std::string &ss, const char *data, size_t len) {
  thread_local std::string res;
  res.clear();

  AppendFormat(res, "%s[%d]:\n", ss.c_str(), (int)len);

  size_t i, j, k;
  for (i = 0; i < len; i += 16) {
    AppendFormat(res, "| ");
    for (j = i, k = 0; k < 16 && j < len; ++j, ++k) {
      AppendFormat(res, "%.2x ", (uint8_t)data[j]);
    }
    for (; k < 16; ++k) {
      AppendFormat(res, "   ");
    }
    AppendFormat(res, "|");
    for (j = i, k = 0; k < 16 && j < len; ++j, ++k) {
      char c = data[j];
      if (!isprint(c) || (c == '\t'))
        c = '.';
      AppendFormat(res, "%c", c);
    }
    for (; k < 16; ++k) {
      AppendFormat(res, " ");
    }
    AppendFormat(res, "|\n");
  }

  printf(" %s", res.c_str());
}

static inline uint16_t CSNTOH16(uint16_t data) {
  return ((data & 0xFF00) >> 8) | ((data & 0x00FF) << 8);
}

const uint8_t *ReadFile(const char *fullPath, uint32_t limitSize) {
  std::ifstream file;
  file.open(fullPath, std::ios::binary);
  if (!file.is_open()) {
    return nullptr;
  }

  file.seekg(0, std::ios::end);
  uint32_t fileSize = file.tellg();
  if (0 != limitSize && fileSize > limitSize) {
    file.close();
    return nullptr;
  }

  file.seekg(0, std::ios::beg);
  uint8_t *content = new uint8_t[fileSize + 5];
  if (content == nullptr) {
    file.close();
    return nullptr;
  }

  // read file content
  *(uint32_t *)content = (uint32_t)fileSize;
  uint32_t rSize = (uint32_t)file.read((char *)content + 4, fileSize).gcount();
  file.close();

  if (rSize != fileSize) {
    delete[] content;
    return nullptr;
  }
  content[rSize + 4] = 0;
  return content;
}

/**
 * @brief       将一个网络字节序的32位数值转换成主机字节序
 * @param       [in] data 要转换的网络字节序的数值
 * @return      返回转换后的主机字节序
 * */
static inline uint32_t CSNTOH32(uint32_t data) {
  return ((data & 0xFF000000) >> 24) | ((data & 0x00FF0000) >> 8) |
         ((data & 0x0000FF00) << 8) | ((data & 0x000000FF) << 24);
}

/**
 * @brief       将一个网络字节序的64位数值转换成主机字节序
 * @param       [in] data 要转换的网络字节序的数值
 * @return      返回转换后的主机字节序
 * */
static inline uint64_t CSNTOH64(uint64_t data) {
  return ((data & 0xFF00000000000000) >> 56) |
         ((data & 0x00FF000000000000) >> 40) |
         ((data & 0x0000FF0000000000) >> 24) |
         ((data & 0x000000FF00000000) >> 8) |
         ((data & 0x00000000FF000000) << 8) |
         ((data & 0x0000000000FF0000) << 24) |
         ((data & 0x000000000000FF00) << 40) |
         ((data & 0x00000000000000FF) << 56);
}

#define CSHTON16 CSNTOH16
#define CSHTON32 CSNTOH32
#define CSHTON64 CSNTOH64

void printHead(const uint8_t *data) {
  KafkaTransBinaryHead *kfkTransBnyHead = (KafkaTransBinaryHead *)data;
  printf("headlen: %u ,ipVersion : %u, reserve: %u,isFirst :%u isDone:%u "
         "netlinkId:%u, dataLen:%u seq:%u pktCount:%u taskId:%u\n",
         kfkTransBnyHead->headerLen, kfkTransBnyHead->ipVersion,
         kfkTransBnyHead->reserve, kfkTransBnyHead->isFirst,
         kfkTransBnyHead->isDone, kfkTransBnyHead->netlinkId,
         CSNTOH32(kfkTransBnyHead->dateLen),
         CSNTOH16(kfkTransBnyHead->sequence),
         CSNTOH16(kfkTransBnyHead->pktCount),
         CSNTOH16(*(uint16_t *)(data + sizeof(KafkaTransBinaryHead) + 4)));
}

static uint8_t *s_data = new uint8_t[1024 * 1024 * 4]{};
static uint32_t s_dataLen = 0;

void procData(const uint8_t *data) {
  KafkaTransBinaryHead *kfkTransBnyHead = (KafkaTransBinaryHead *)data;
  uint32_t dataLen = CSNTOH32(kfkTransBnyHead->dateLen);
  mempcpy(s_data + s_dataLen, data + sizeof(KafkaTransBinaryHead) + 4 + 2,
          dataLen - 2);
  s_dataLen += dataLen - 2;
  printf("proc %u %u\n", dataLen, s_dataLen);
}


int main(int argc, char *argv[]) {

  {
    const uint8_t *content =
        ReadFile("/cppfile/kafka_16.log2", 0);
    if (nullptr != content) {
      uint32_t bufferLen = *(uint32_t *)content;
      auto *data = (content + sizeof(bufferLen));
      printHead(data);
      procData(data);

      PrintData("data1", (const char *)data, bufferLen);

      delete[] content;
    }
  }
  {
    const uint8_t *content =
        ReadFile("/cppfile/kafka_16.log2", 0);
    if (nullptr != content) {
      uint32_t bufferLen = *(uint32_t *)content;
      printHead((content + sizeof(bufferLen)));
      PrintData("data2", (const char *)(content + sizeof(bufferLen)),
                bufferLen);
      procData((content + sizeof(bufferLen)));
      delete[] content;
    }
  }
  {
    const uint8_t *content =
        ReadFile("/cppfile/kafka_16.log2", 0);
    if (nullptr != content) {
      uint32_t bufferLen = *(uint32_t *)content;
      printHead((content + sizeof(bufferLen)));
      PrintData("data2", (const char *)(content + sizeof(bufferLen)),
                bufferLen);
      procData((content + sizeof(bufferLen)));
      delete[] content;
    }
  }
  uint32_t pos = 0;
  uint32_t totalSize = 0;
  struct Test{
    uint32_t type;
    std::string namer;
  };

  std::vector<Test> fieldT;
  if (s_dataLen > 0) {
    if (pos + 2 > s_dataLen) {
      printf("Error: Invalid data length when reading field count\n");
      return 1;
    }
    
    uint16_t fieldCount = CSNTOH16(*(uint16_t *)s_data);
    pos += 2;

    printf("fieldCount = %u\n ", fieldCount);
    
    if (fieldCount > 1000) {
      printf("Error: Field count too large: %u\n", fieldCount);
      return 1;
    }

    while (fieldCount && pos < s_dataLen) {
      if (pos + 4 > s_dataLen) {
        printf("Error: Invalid data length when reading field length\n");
        return 1;
      }
      
      uint32_t len = CSNTOH32(*(uint32_t *)(s_data + pos));
      if (len == 0 || len > 254 || pos + 4 + len > s_dataLen) {
        printf("Error: Invalid field name length: %u at position %u\n", len, pos);
        return 1;
      }
      pos += 4;

      char fieldName[255] = {};
      bool valid = true;
      for (uint32_t i = 0; i < len; i++) {
        char c = *(s_data + pos + i);
        if (!isprint(c) && c != '\0') {
          valid = false;
          break;
        }
      }
      
      if (!valid) {
        printf("Error: Invalid characters in field name at position %u\n", pos);
        return 1;
      }
      
      memcpy(fieldName, s_data + pos, len);
      fieldName[len] = '\0';
      pos += len;
      
      if (pos + 1 > s_dataLen) {
        printf("Error: Cannot read field type at position %u\n", pos);
        return 1;
      }
      
      uint8_t fieldType = *(s_data + pos);
      pos += 1;
      
      if (fieldType < 1 || fieldType > 12) {
        printf("Error: Invalid field type: %u at position %u\n", fieldType, pos - 1);
        return 1;
      }
      
      printf("field: %s (len=%u) type=%u pos=%u\n", fieldName, len, fieldType, pos);
      
      try {
        fieldT.push_back({fieldType, std::string(fieldName, len)});
      } catch (const std::exception& e) {
        printf("Error: Failed to store field info: %s\n", e.what());
        return 1;
      }
      
      --fieldCount;
    }

    if (fieldCount > 0) {
      printf("Error: Not all fields were read successfully\n");
      return 1;
    }

    if (pos + 19 > s_dataLen) {
      printf("Error: Not enough data for netlink information\n");
      return 1;
    }

    uint8_t netlinkCount = *(uint8_t *)(s_data + pos);
    pos += 1;
    uint16_t netlikId = CSNTOH16(*(uint16_t *)(s_data + pos));
    pos += 2;

    uint32_t netlikRecord = CSNTOH32(*(uint32_t *)(s_data + pos));
    pos += 4;

    uint64_t netlinkTime = CSNTOH64(*(uint64_t *)(s_data + pos));
    pos += 8;

    uint32_t teimRecord = CSNTOH32(*(uint32_t *)(s_data + pos));
    pos += 4;

    printf("%u %u %u %lu %u\n", netlinkCount, netlikId, netlikRecord,
           netlinkTime, teimRecord);

    while (teimRecord && pos < s_dataLen) {
      --teimRecord;
      for (const auto& f : fieldT) {
        printf("%s %u %u ", f.namer.c_str(), f.type, pos);
        
        switch (f.type) {
        case 1:
        case 12:
          if (pos + 1 > s_dataLen) goto error_exit;
          printf("%u\n", *(s_data + pos));
          pos += 1;
          break;
          
        case 2:
          if (pos + 2 > s_dataLen) goto error_exit;
          printf("%u\n", CSNTOH16(*(uint16_t*)(s_data + pos)));
          pos += 2;
          break;
          
        case 3:
        case 8:
          if (pos + 4 > s_dataLen) goto error_exit;
          printf("%u\n", CSNTOH32(*(uint32_t*)(s_data + pos)));
          pos += 4;
          break;
          
        case 4:
        case 5:
        case 6:
        case 9:
          if (pos + 8 > s_dataLen) goto error_exit;
          printf("%lu\n", CSNTOH64(*(uint64_t*)(s_data + pos)));
          pos += 8;
          break;
          
        case 7: {
          if (pos + 4 > s_dataLen) goto error_exit;
          uint32_t len = CSNTOH32(*(uint32_t *)(s_data + pos));
          if (len > s_dataLen - pos - 4) goto error_exit;
          printf("%u\n", len);
          pos += 4;
          pos += len;
        } break;
          
        case 10: {
          if (pos + 1 > s_dataLen) goto error_exit;
          uint8_t ver = *(s_data + pos);
          pos += 1;
          if (ver == 4) {
            if (pos + 4 > s_dataLen) goto error_exit;
            pos += 4;
          } else if (ver == 6) {
            if (pos + 16 > s_dataLen) goto error_exit;
            pos += 16;
          } else {
            printf("Error: Invalid IP version: %u\n", ver);
            goto error_exit;
          }
          printf("%u\n", ver);
        } break;
          
        case 11: {
          if (pos + 4 > s_dataLen) goto error_exit;
          int32_t len = (*(int32_t *)(s_data + pos));
          if (len < 0 || pos + 4 + (uint32_t)len > s_dataLen) goto error_exit;
          printf("%d %d\n", len, *(s_data + pos + 4));
          pos += 4;
          pos += (uint32_t)len;
        } break;
          
        default:
          printf("Error: Unknown field type: %u\n", f.type);
          goto error_exit;
        }
      }
    }

    printf("%u %u %u\n", teimRecord, pos, s_dataLen);
  }

  return 0;
}
