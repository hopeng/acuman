if [ "$1" = "clean" ]
then
    ./gradlew clean build
fi

nohup java -jar build/libs/acuman-all.jar >> server.log 2>&1 &