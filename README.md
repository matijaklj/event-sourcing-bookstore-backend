# Event Sourcing with Kumuluz-Streaming extension
description...

## How to use

set up zookeeper and kafka
```bash
$ docker network create kafka-net
  
$ docker run -d -p 2181:2181 --name zookeeper --network kafka-net zookeeper:3.4
$ docker run -d -p 9092:9092 --name kafka --network kafka-net --env ZOOKEEPER_IP=zookeeper ches/kafka
```

create topics that are needed for the app:

* commandsTopic ~ topics for the commands
* bookEventsTopic ~ topics for the event about the book
* booksTopic ~ topics with books ??
* shipmentsEventsTopic ~ topics with events about shipments

```bash
docker run --rm --network kafka-net ches/kafka kafka-topics.sh --create --topic commandsTopic --replication-factor 1 --partitions 1 --zookeeper zookeeper:2181

docker run --rm --network kafka-net ches/kafka kafka-topics.sh --create --topic bookEventsTopic --replication-factor 1 --partitions 1 --zookeeper zookeeper:2181

docker run --rm --network kafka-net ches/kafka kafka-topics.sh --create --topic booksTopic --replication-factor 1 --partitions 1 --zookeeper zookeeper:2181

docker run --rm --network kafka-net ches/kafka kafka-topics.sh --create --topic shipmentsEventsTopic --replication-factor 1 --partitions 1 --zookeeper zookeeper:2181

```

2. Build the sample using maven:
   
   ```bash
   $ cd catalogue
   $ mvn clean package
   ```

3. Run the sample:

    ```bash
    $ java -cp target/classes:target/dependency/* com.kumuluz.ee.EeApplication
    ```

    in Windows environment use the command
    ```batch
    java -cp target/classes;target/dependency/* com.kumuluz.ee.EeApplication
    ```