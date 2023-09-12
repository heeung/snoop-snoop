# TIL_0904

## Apache spark란?
Apache Spark는 데이터 처리와 분석을 위한 오픈 소스 클러스터 컴퓨팅 프레임워크입니다. Spark는 대규모 데이터 집합을 처리하고 분석하는 데 사용되며, 분산 환경에서 빠르고 효율적인 데이터 처리를 지원합니다. 

## pyspark란?
PySpark는 Apache Spark를 Python 프로그래밍 언어로 사용하기 위한 라이브러리입니다. 또한, PySpark는 DataFrame API를 제공하여 구조화된 데이터를 처리하기가 편리합니다. DataFrame은 SQL 스타일의 쿼리와 데이터 조작을 지원하여 데이터 처리 작업을 단순화합니다.

## Pyspark 설치

```pip install pyspark```

## Pyspark 예제

```python
from pyspark.sql import SparkSession

# 스파크 세션 생성
spark = SparkSession.builder.master("local").appName("SparkSQL").getOrCreate()
# 로그 레벨 정의
spark.sparkContext.setLogLevel("ERROR")
```

```
# 예제
data = [('001','Smith','M',40,'DA',4000),
        ('002','Rose','M',35,'DA',3000),
        ('003','Williams','M',30,'DE',2500),
        ('004','Anne','F',30,'DE',3000),
        ('005','Mary','F',35,'BE',4000),
        ('006','James','M',30,'FE',3500)]

columns = ["cd","name","gender","age","div","salary"]
df = spark.createDataFrame(data = data, schema = columns)
```

```
>> df.show()
+---+--------+------+---+---+------+
| cd|    name|gender|age|div|salary|
+---+--------+------+---+---+------+
|001|   Smith|     M| 40| DA|  4000|
|002|    Rose|     M| 35| DA|  3000|
|003|Williams|     M| 30| DE|  2500|
|004|    Anne|     F| 30| DE|  3000|
|005|    Mary|     F| 35| BE|  4000|
|006|   James|     M| 30| FE|  3500|
+---+--------+------+---+---+------+
```

# TIL_0905

## Elastic Search Docker 빌드

Elastic Search 설치

```docker pull docker.elastic.co/elasticsearch/elasticsearch:7.9.1```

Elastic Search 실행

```
docker run -d -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name elasticsearch7 docker.elastic.co/elasticsearch/elasticsearch:7.9.1
b5028c898638f74a5aef326899617503b1ef74ee53597cd6c23f50182e4435c6
```

## Elastic Search 사용

Elastic Search 클러스터 연결

```es = Elasticsearch([{'host': 'localhost', 'port': 9200, 'scheme': 'http'}])```

Elastic Search에 데이터 인덱싱

```es.index(index='coupang_data', body=json.dumps(row.to_dict()), headers={"accept": "application/json", "content-type": "application/json"})```

Elastic Search 클러스터 종료

```es.close()```

# TIL_0906

## ELK란 무엇일까?
ELK는 3가지 오픈 소스 소프트웨어인 분석 및 저장 기능을 담당하는 ElasticSearch, 수집 기능을 하는 Logstash, 시각화 도구인 Kibana의 각각의 앞글자를 따서 만든 단어로 각 제품이 연동되어 데이터 수집 및 분석 툴로 활용됩니다.

## Logstash
Logstash는 크게 입력 -> 필터 -> 출력과 같이 3가지 단계로 이루어집니다.

입력에서는 다양한 데이터 저장소로부터 데이터를 입력받는 작업을 진행합니다.

필터에서는 데이터를 확장, 변경, 필터링 및 삭제 처리하는 가공 작업을 진행합니다.

출력에서는 다양한 데이터 저장소로 데이터를 전송하는 작업을 진행합니다.

## ElasticSearch
ElasticSearch는 Lucene 기반으로 개발된 분산 검색 엔진입니다.

ELK 구조에서의 역할은 Logstash를 통해 수신된 데이터를 ElasticSearch에 저장하는 데이터 저장소 역할을 수행합니다.

NoSQL 기반 데이터베이스이며 RDB와 다르게 트랜잭션 Rollback을 지원하지 않으나 검색 및 분석 성능이 뛰어납니다.

## Kibana

Kibana는 사용자에게 분석 결과를 시각화 해주는 소프트웨어로 ElasticSearch에 저장된 데이터들을 시각화하여 차트, 그래프화 또는 로그 데이터를 한번에 모아서 볼 수 있습니다.

# TIL_0907

## Docker Compose란?
이번 프로젝트에서는 springboot, mysql, kafka, zookeeper, elasticsearch 등등과 같이 다양한 포트들을 사용하기 때문에 Docker compose를 활용해 원활한 운영을 하고자 합니다.

Docker Compose는 멀티 컨테이너 Docker 애플리케이션을 정의하고 실행하기 위한 도구입니다. 하나 이상의 도커 컨테이너로 구성된 애플리케이션을 정의하고 실행하기 위한 YAML 파일을 사용하여 다수의 서비스 및 환경 설정을 관리할 수 있습니다.

## Docker Compose 특징 
복잡한 애플리케이션 관리: Docker Compose를 사용하면 복잡한 애플리케이션을 쉽게 관리할 수 있습니다. 여러 개의 컨테이너로 구성된 애플리케이션의 실행, 중지, 로깅, 네트워크 연결 및 스케일링을 간단한 명령으로 수행할 수 있습니다.

여러 컨테이너 간의 의존성 관리: 애플리케이션 내의 서비스 및 컨테이너 간에 의존성이 있을 때, Docker Compose는 이러한 의존성을 관리하고 정의할 수 있습니다. 이것은 서비스가 다른 서비스를 필요로 할 때 컨테이너의 실행 순서와 종속성을 제어할 수 있음을 의미합니다.

동일한 환경 재현: Docker Compose를 사용하면 개발, 테스트, 스테이징, 프로덕션 환경 등 여러 환경에서 동일한 애플리케이션을 실행하고 재현하는 데 도움이 됩니다. 이로써 버그 및 호환성 문제를 미리 발견할 수 있습니다.

개발 환경 구축: 개발자는 Docker Compose를 사용하여 개발 환경을 쉽게 구축할 수 있습니다. 프로젝트에 필요한 모든 서비스와 종속성을 정의하고 단일 명령으로 환경을 실행할 수 있으므로 개발 생산성을 향상시킬 수 있습니다.

스케일링: Docker Compose를 사용하면 서비스를 복제하여 스케일링할 수 있습니다. 예를 들어, 웹 서버 서비스를 여러 개의 인스턴스로 확장하려면 몇 줄의 코드만 추가하면 됩니다.

## Docker Compose 파일 작성
```java
version: '3.3'
services:
  zookeeper:
    container_name: zookeeper-cntr
    image: confluentinc/cp-zookeeper:7.2.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    networks:
      - kafka_elk_nw

  kafka:
    container_name: kafka-cntr
    image: confluentinc/cp-kafka:7.2.0
    depends_on:
      - zookeeper
    ports:
      - 29092:29092
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper-cntr:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-cntr:9092,PLAINTEXT_HOST://localhost:29092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_ADVERTISED_HOST_NAME: localhost
    networks:
      - kafka_elk_nw
    healthcheck:
      test: nc -vz kafka-cntr 9092 || exit -1
      # start_period: 15s
      interval: 5s
      timeout: 10s
      retries: 10
  
  elasticsearch:
    container_name: elasticsearch-cntr
    image: elasticsearch:7.9.1
    environment:
      - cluster.name=kafka-cluster
      - bootstrap.memory_lock=true
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
      - xpack.security.enabled=false
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data:rw
    ulimits:
      memlock:
        soft: -1
        hard: -1
    ports:
      - 9200:9200
      - 9300:9300
    depends_on:
      - kafka
    stdin_open: true
    tty: true
    restart: always
    networks:
      - kafka_elk_nw
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "50"
    healthcheck:
      test: curl -u elastic:elastic -s -f elasticsearch-cntr:9200/_cat/health >/dev/null || exit 1
      interval: 10s
      timeout: 10s
      retries: 5
  logstash:
    container_name: logstash-cntr
    image: logstash:7.9.1
    ulimits:
      memlock:
        soft: -1
        hard: -1
    volumes:
      - ./logstash-kafka.conf:/usr/share/logstash/pipeline/logstash-kafka.conf
    ports:
      - 5044:5044
    depends_on:
      - elasticsearch
    stdin_open: true
    tty: true
    restart: always
    networks:
      - kafka_elk_nw
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "50"
    healthcheck:
      test: ["CMD", "curl", "--silent", "--fail", "http://logstash-cntr:9600"]
      interval: 30s
      timeout: 15s
      retries: 3
  kibana:
    container_name: kibana-cntr
    image: kibana:7.9.1
    ulimits:
      memlock:
        soft: -1
        hard: -1
    ports:
      - 5601:5601
    depends_on:
      - elasticsearch
    stdin_open: true
    tty: true
    restart: always
    networks:
      - kafka_elk_nw
    links: ['elasticsearch']
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "50"
    healthcheck:
      test: curl --fail http://kibana-cntr:5601 || exit 1
      interval: 30s
      retries: 3
      timeout: 10s

networks:
  kafka_elk_nw:
    driver: bridge

volumes:
  elasticsearch_data:
    driver: local
```

### Docker Compose 관리

실행

```docker-compose up```

실행 종료

```docker-compose down```

# TIL_0908

## Springboot, Docker를 활용한 배포

Docker를 사용해서 Springboot를 배포하기 위해서는 Springboot 패키지에 Dockerfile을 작성해야합니다.

이는 배포 프로세스를 단순화하기 위함입니다.

Dockerfile에 작성된 자바 정보와 jar 파일 정보를 통해 docker 이미지를 생성합니다. 

```
FROM openjdk:11
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Dockerfile 작성 후 해당 디렉토리 레벨에서 docker build를 진행합니다. 그러면 이미지가 생성되며 해당 이미지를 통해 docker container를 활성화 시킵니다.

로컬에서 build한 이미지는 docker hub애 push하고 EC2 환경에서 다시 hub의 이미지를 pull 하여 EC2 환경에서 docker container가 활성화되도록 합니다.

# TIL_0911

## 개요
이번 프로젝트에서 파이썬을 활용한 크롤러를 사용하기 때문에 크롤러를 위한 서버가 필요하다 생각했습니다. Django, flask, FastAPI 다양한 파이썬 서버가 있었지만, 간단하고 빠른 Fast api를 사용해 서버를 구축하고자 합니다.

## FastAPI란?

FastAPI는 파이썬을 기반으로 하는 모던하고 빠른 웹 애플리케이션 프레임워크입니다. FastAPI는 간단한 문법, 빠른 성능, 자동 문서화, 데이터 유효성 검사 및 비동기 지원과 같은 많은 기능을 제공하여 웹 개발을 쉽게 만들어줍니다.

FastAPI는 성능 최적화에 중점을 둔 웹 프레임워크로, ASGI (Asynchronous Server Gateway Interface)를 기반으로 하여 비동기 프로그래밍을 지원합니다. 이로써 빠른 속도와 높은 동시성 처리를 가능하게 합니다.

## FastAPI 사용법

```python
from fastapi import FastAPI

# fast api 서버
app = FastAPI()

@app.get("/")
async def root():
    return {"message": "Hello World"}
```
위의 FastAPI() 함수를 통해 서버를 실행할 수 있습니다.

또한, async를 활용해 해당하는 http method에 따라 비동기 처리가 가능합니다.

# TIL_0912

## Python을 활용한 Kafka 연결

``` python
import json
from confluent_kafka import Producer, KafkaError, KafkaException

def send_to_kafka(products_info):
    # Kafka 프로듀서 설정
    producer_config = {
        'bootstrap.servers': 'j9d104a.p.ssafy.io:9092',  # Kafka 브로커의 주소
    }

    producer = Producer(producer_config)

    # Kafka 토픽 설정
    kafka_topic = 'coupang'  # 보내려는 Kafka 토픽 이름

    try:
        # 데이터를 JSON 형식으로 직렬화하여 Kafka 토픽으로 보냄
        producer.produce(kafka_topic, key=None, value=json.dumps(products_info, ensure_ascii=False).encode('utf-8'))

        # 메시지 전송 확인
        producer.poll(0)
        producer.flush()

    except KafkaException as e:
        if e.args[0].code() == KafkaError._INVALID_ARG:
            print(f"Invalid argument: {e}")
        else:
            print(f"Failed to send message to Kafka: {e}")
    except Exception as e:
        print(f"An error occurred: {e}")
    finally:
        producer.flush()
```
