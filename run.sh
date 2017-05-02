if [ "$1" = "clean" ]
then
    ./gradlew clean build
fi

sudo iptables -A PREROUTING -t nat -i eth0 -p tcp --dport 80 -j REDIRECT --to-port 4567


nohup java -jar build/libs/acuman-all.jar >> server.log 2>&1 &
