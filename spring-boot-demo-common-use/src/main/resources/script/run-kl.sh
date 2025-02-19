#!/bin/bash

# 脚本名称: run-kelai.sh
# 功能: 启动、停止、重启或查询 kelai-binary-parse-0.0.1-SNAPSHOT.jar 的状态
# 使用方法:
#   ./run-kelai.sh start --spring.profiles.active=prod
#   ./run-kelai.sh stop
#   ./run-kelai.sh restart --spring.profiles.active=dev
#   ./run-kelai.sh status

# JAR 包名称
JAR_NAME="kelai-binary-parse-0.0.1-SNAPSHOT.jar"

# 日志目录
LOG_DIR="logs"
LOG_FILE="$LOG_DIR/kelai-binary-parse.log"

# PID 文件路径
PID_FILE="kelai-binary-parse.pid"

# 创建日志目录
mkdir -p $LOG_DIR

# 启动函数
start() {
    if [ -f $PID_FILE ]; then
        echo "应用程序已经在运行中，PID: $(cat $PID_FILE)"
        exit 1
    fi

    # 检查指定的 spring.profiles.active 参数并验证对应配置文件是否存在
    for arg in "$@"; do
        if [[ $arg == -spring.profiles.active=* ]]; then
            PROFILE=${arg#*=}
            check_profile $PROFILE
        fi
    done

    echo "正在启动应用程序..."
    nohup java -jar $JAR_NAME "$@" > $LOG_FILE 2>&1 &
    echo $! > $PID_FILE
    echo "应用程序已启动，PID: $(cat $PID_FILE)"
}


check_profile() {
    PROFILE=$1
    CONFIG_FILE="application-$PROFILE.properties"
    if [ ! -f $CONFIG_FILE ]; then
        echo "配置文件 $CONFIG_FILE 不存在，请检查路径和名称是否正确。"
        exit 1
    fi
    echo "找到配置文件 $CONFIG_FILE"
}



# 停止函数
stop() {
    if [ ! -f $PID_FILE ]; then
        echo "应用程序未运行"
        exit 1
    fi

    echo "正在停止应用程序..."
    PID=$(cat $PID_FILE)
    kill $PID
    rm -f $PID_FILE
    echo "应用程序已停止"
}

# 重启函数
restart() {
    stop
    # 只传递 Spring 配置参数给 start 函数
    start "$@"
}

# 查询状态函数
status() {
    if [ -f $PID_FILE ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null; then
            echo "应用程序正在运行，PID: $PID"
        else
            echo "应用程序已停止，但 PID 文件仍存在"
            rm -f $PID_FILE
        fi
    else
        echo "应用程序未运行"
    fi
}

# 根据输入参数调用对应函数
case "$1" in
    start)
        shift  # 移除第一个参数（start）
        start "$@"  # 只传递 Spring 配置参数
        ;;
    stop)
        stop
        ;;
    restart)
        shift  # 移除第一个参数（restart）
        restart "$@"  # 只传递 Spring 配置参数
        ;;
    status)
        status
        ;;
    *)
        echo "使用方法: $0 {start|stop|restart|status} [Spring 配置参数]"
        exit 1
        ;;
esac
