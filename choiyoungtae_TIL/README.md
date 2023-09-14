# TIL-23.09.04

## docker-compose를 통한 kafka 클러스터 구축

### 1. EC2 접속 후 도커 컴포즈 설치
```bash
$ sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
$ sudo chmod +x /usr/local/bin/docker-compose
```

### 2. docker-compose 파일 작성
```bash
# 폴더 생성
$ mkdir ./kafka
# compose 파일 작성
$ vi docker-compose.yml
```

```html
# docker-compose.yml
---
version: '3.8'
services:
zookeeper-1:
image: confluentinc/cp-zookeeper:latest
ports:
- '32181:32181'
environment:
ZOOKEEPER_CLIENT_PORT: 32181
ZOOKEEPER_TICK_TIME: 2000
>
kafka-1:
image: confluentinc/cp-kafka:latest
ports:
- '9092:9092'
depends_on:
- zookeeper-1
environment:
KAFKA_BROKER_ID: 1
KAFKA_ZOOKEEPER_CONNECT: zookeeper-1:32181
KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT
KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka-1:29092,EXTERNAL://localhost:9092
KAFKA_DEFAULT_REPLICATION_FACTOR: 3
KAFKA_NUM_PARTITIONS: 3
>
kafka-2:
image: confluentinc/cp-kafka:latest
ports:
- '9093:9093'
depends_on:
- zookeeper-1
environment:
KAFKA_BROKER_ID: 2
KAFKA_ZOOKEEPER_CONNECT: zookeeper-1:32181
KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT
KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka-2:29093,EXTERNAL://localhost:9093
KAFKA_DEFAULT_REPLICATION_FACTOR: 3
KAFKA_NUM_PARTITIONS: 3
>
kafka-3:
image: confluentinc/cp-kafka:latest
ports:
- '9094:9094'
depends_on:
- zookeeper-1
environment:
KAFKA_BROKER_ID: 3
KAFKA_ZOOKEEPER_CONNECT: zookeeper-1:32181
KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT
KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka-3:29094,EXTERNAL://localhost:9094
KAFKA_DEFAULT_REPLICATION_FACTOR: 3
KAFKA_NUM_PARTITIONS: 3
```
- kafka 브로커 3개로 구성된 클러스터 생성
- 포트는 9092, 9093, 9094로 설정 (ec2 인스턴스 보안설정에서도 개방해줘야한다)


### 3. docker-compose-kafka-ui
- 모니터링을 위한 kafka-ui 파일도 작성
```bash
$ vi docker-compose-kafka-ui.yml
```
 ```html
version: '2'
services:
kafka-ui:
image: provectuslabs/kafka-ui
container_name: kafka-ui
ports:
- "8989:8080"
restart: always
environment:
- KAFKA_CLUSTERS_0_NAME=local
- KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=kafka-1:29092,kafka-2:29093,kafka-3:29094
- KAFKA_CLUSTERS_0_ZOOKEEPER=zookeeper-1:22181
```

**docker 실행**
```bash
# 작성한 위치에서 도커 실행
$ docker-compose up -d
```

### 4. kafka-ui를 통해 모니터링
지정해준 8989 포트로 접속<br>
localhost:8989
![img.png](image/img.png)
> online 상태인 클러스터에 브로커 3개가 잘 생성된 것을 확인할 수 있다.


<br>

## 크롤링 데이터 kafka로 실시간 전송

- ec2 인스턴스 내에서 파이썬 파일을 작성해서 localhost:9092로 전송
- 쿠팡에서 검색어 입력 후 상품목록 3페이지정도를 크롤링
- kafka의 producer역할

```bash
# 파이썬 크롤링 파일 작성
$ vi crawling.py
```

```python
import requests
import re
from bs4 import BeautifulSoup
import json
from kafka import KafkaProducer

# Kafka 설정
producer = KafkaProducer(bootstrap_servers='localhost:9092')

HEADERS = {
"User-Agent": "...",
"Accept-Language": "ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3"
}

SEARCH_PRODUCT_CLASS = re.compile("^search-product")

for i in range(1, 4):
#print("페이지 :", i)
url = "https://www.coupang.com/np/search?component=&q=%EC%95%84%EC%9D%B4%ED%8C%A8%EB%93%9C+%EC%97%90%EC%96%B4+5&channel=auto"

    try:
        res = requests.get(url, headers=HEADERS)
        res.raise_for_status()
    except requests.RequestException as e:
        print(f"Request failed: {e}")
        continue

    soup = BeautifulSoup(res.text, "html.parser")
    items = soup.find_all("li", attrs={"class": SEARCH_PRODUCT_CLASS})

    for item in items:

        # 광고 제품은 제외
        ad_badge = item.find("span", attrs={"class":"ad-badge-text"})
        if ad_badge:
            #print("  <광고 상품 제외합니다>")
            continue

        name = item.find("div", attrs={"class":"name"}).get_text() # 제품명

        price_tag = item.find("strong", attrs={"class": "price-value"})
        if price_tag:
            price = price_tag.get_text()
        else:
            print("가격 정보를 찾을 수 없습니다.")
            continue

        # 리뷰 10개 이상, 평점 3 이상 되는 것만 조회
        rate = item.find("em", attrs={"class":"rating"}) # 평점
        if rate:
            rate = rate.get_text()
        else:
            #rate = "평점 없음"
            #print("  <평점 없는 상품 제외합니다>")
            continue

        rate_cnt = item.find("span", attrs={"class":"rating-total-count"}) # 평점 수
        if rate_cnt:
            rate_cnt = rate_cnt.get_text()[1:-1] # 예 : (26), 괄호 없애기

        else:
            #rate_cnt = "평점 수 없음"
            #print("  <평점 수 없는 상품 제외합니다>")
            continue

        link = item.find("a", attrs={"class":"search-product-link"})["href"]

        if float(rate) >= 3 and int(rate_cnt) >= 10:
            #print(name, price, rate, rate_cnt)
            #print(f"제품명 : {name}")
            #print(f"가격 : {price}")
            #print(f"평점 : {rate}점 ({rate_cnt})개")
            #print("바로가기 : {}".format("https://www.coupang.com/"+link))
            #print("-"*100)
            # 전송 데이터 형식
            product_info = {
                "name": name,
                "price": price,
                "rate": rate,
                "rate_cnt": rate_cnt,
                "link": f"https://www.coupang.com/{link}"
            }
            # kafka로 데이터 전송
            producer.send('product_topic', value=json.dumps(product_info).encode('utf-8'))
```

![img_1.png](image/img_1.png)
> 크롤링 데이터가 메시지로 kafka에 전송된 것을 확인

<br>

# TIL-23.09.05

## ELK 스택

### 1. ElasticSearch
> - 데이터 관리 (저장/검색/분석)
> - 검색 엔진: 실시간 분산 검색 엔진으로, 대규모 데이터를 빠르게 저장하고 검색할 수 있다
> - JSON 기반: 데이터를 JSON 형식으로 색인화
> - 분산처리: 여러 노드와 클러스터로 쉽게 확장
> - RESTful API: 다양한 프로그래밍 언어로 쉽게 접근할 수 있는 RESTful API를 제공

### 2. Logstash
> - 데이터 처리 파이프라인
> - 데이터 수집과 변환: 다양한 소스에서 로그 또는 이벤트 데이터를 수집하여 필요에 따라 변환
> - 플러그인 아키텍처: 입력, 필터, 출력 플러그인을 통해 다양한 데이터 소스와 목적지에 연결
> - Elasticsearch와 통합: 일반적으로 Logstash는 Elasticsearch로 데이터를 전송하여 색인을 생성하거나 업데이트

### 3. Kibana
> - 시각화 기능
> - 데이터 시각화: Elasticsearch에서 저장된 데이터를 기반으로 다양한 차트나 대시보드를 만들 수 있다
> - 데이터 탐색: 저장된 데이터를 쉽게 검색하고 탐색
> - Dev Tools: Elasticsearch의 RESTful API를 직접 사용하여 데이터를 질의할 수 있는 기능을 제공

### 4. Beats
> - 경량 로그 수집기: Logstash가 복잡한 데이터 변환 작업을 수행하는 데 반해, Beats는 보다 경량화되어 있고, 특정 유형의 데이터를 빠르게 수집하는 데 초점을 맞춘다
> - 다양한 베리언트: Filebeat(로그 파일), Metricbeat(메트릭), Packetbeat(네트워크 데이터), Auditbeat(보안 감사 등)과 같이 특정 데이터 유형에 특화된 여러 베리언트가 있다
> - Elasticsearch 및 Logstash 지원: Beats는 직접 Elasticsearch로 데이터를 전송할 수 있으며, 더 복잡한 처리를 위해 Logstash로도 데이터를 보낼 수 있다
> - 간단한 배포와 확장: 각 서버에 작은 에이전트를 설치하기만 하면 되므로, 배포와 확장이 매우 쉽다

<br>

# TIL-23.09.06

### 데이터 분산 프로세스 설계

> 1. 쿠팡, 11번가에서 크롤링 및 API를 통해 상품 데이터 수집
> 2. 수집된 데이터는 kafka producer 역할을 하며 카테고리를 토픽으로 생성
> 3. kafka 클러스터는 docker-compose를 통해 3개의 브로커로 구축
> 4. kafka connect로 실시간 업데이트되는 컨텐츠를 감시하고 logstash로 전송
> 5. logstash에서 데이터 포맷 통일 후 Elasticsearch로 전송
> 6. ElasticSearch에서 데이터 저장 및 관리

<br>

# TIL-23.09.07

### ELK 스택 구축과 kafka connect 설정

#### 1. ELK 스택

> 도커 컴포즈를 통해서 3개의 elasticsearch 노드와 kibana, logstash를 띄우려고 했지만 프리티어 인스턴스의 스펙이 너무 낮아 싱글노드의 elasticsearch와 각각의 도커 명령어를 통해서 실행시켰다<br>
실행 전 인스턴스의 보안그룹에서 포트 열어주는 것 잊지말자

```bash
# elasticsearch 실행
$ docker run --name elasticsearch -d -e "discovery.type=single-node" -e "ES_JAVA_OPTS=-Xms256m -Xmx256m" -p 9200:9200 docker.elastic.co/elasticsearch/elasticsearch:7.14.0
# kibana 실행
$ docker pull docker.elastic.co/kibana/kibana:7.14.0
$ docker run -d --link elasticsearch:elasticsearch -p 5601:5601 --name kibana docker.elastic.co/kibana/kibana:7.14.0
# logstash 실행
$ docker pull docker.elastic.co/logstash/logstash:7.14.0
$ docker run -d --link elasticsearch:elasticsearch -p 5044:5044 --name logstash docker.elastic.co/logstash/logstash:7.14.0
```

#### 2. kafka connect
Kafka와 외부 데이터 소스나 저장소 간에 데이터를 효율적으로 이동시키기 위한 프레임워크<br> 이를 통해 kafka 토픽에 저장되는 데이터를 실시간으로 감지해서 추출할 수 있다.

>- 플러그 가능한 아키텍처: Kafka Connect는 다양한 소스와 싱크를 위한 플러그인(커넥터)을 지원한다. 커넥터는 오픈소스로 제공되거나 커뮤니티나 개발자에 의해 만들어질 수 있다.
>- 분산 및 확장성: Kafka Connect는 분산 환경에서 실행될 수 있다. 이는 데이터 처리량이 늘어나거나 시스템이 복잡해져도 쉽게 확장 가능하다는 것을 의미
>- 오프셋 관리: 내부적으로 Kafka에서 오프셋을 관리하기 때문에, 장애가 발생했을 때도 데이터 무결성을 유지할 수 있다.
>- 설정 기반의 실행: 코드를 작성할 필요 없이 간단한 JSON 또는 YAML 파일로 Kafka Connect를 설정할 수 있다.
>- 스트림과 배치 모드 지원: Kafka Connect는 실시간 스트림 처리뿐만 아니라, 배치 작업도 지원<br>
  결과적으로, Kafka Connect를 이용하면 코드를 작성하지 않고 설정만으로 다양한 외부 시스템과 Kafka 간의 데이터 이동을 처리할 수 있어 운영적인 편의성이 높다.

**Kafka connect vs Kafka connector**
> - **connect**는 외부 시스템과 kafka의 데이터 이동 작업을 처리하는 **프레임워크**
>- **connector**는 connect 프레임워크 안에서 실행되는 **플러그인**으로 로직을 담당, 커넥터는 소스 커넥터와 싱크 커넥터로 구분된다.
  <br>
>- 소스 커넥터(Source Connector): 외부 시스템(예: RDBMS, 로그 파일, 외부 API 등)에서 데이터를 가져와 Kafka 토픽으로 전달
>- 싱크 커넥터(Sink Connector): Kafka 토픽에서 데이터를 가져와 외부 시스템(예: RDBMS, ElasticSearch, HDFS 등)으로 전달

**Kafka Connect 설치 및 실행**
> - ec2 인스턴스 환경에서 도커 컴포즈를 통해 kafka 클러스터를 만들어 놓은 뒤의 작업과정임을 유의
>- kafka 패키지의 일부이므로 따로 설치할 필요없이 설정 파일 수정을 통해 실행 가능

```
kafka-connect:
    image: confluentinc/cp-kafka-connect:latest
    ports:
      - '8083:8083'
    depends_on:
      - kafka-1
      - kafka-2
      - kafka-3
      - zookeeper-1
    environment:
      CONNECT_BOOTSTRAP_SERVERS: kafka-1:29092,kafka-2:29093,kafka-3:29094
      CONNECT_REST_ADVERTISED_HOST_NAME: kafka-connect
      CONNECT_REST_PORT: 8083
      CONNECT_GROUP_ID: "kafka-connect-group"
      CONNECT_CONFIG_STORAGE_TOPIC: "kafka-connect-configs"
      CONNECT_OFFSET_STORAGE_TOPIC: "kafka-connect-offsets"
      CONNECT_STATUS_STORAGE_TOPIC: "kafka-connect-status"
      CONNECT_KEY_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
      CONNECT_VALUE_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
      CONNECT_INTERNAL_KEY_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
      CONNECT_INTERNAL_VALUE_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
```
docker-compose 파일에 위의 kafka connect 내용 추가 후 실행

**elasticsearch와 연결**

- Kafka Connect Elasticsearch 플러그인 설치
```bash
# confluent-hub를 통해 설치
$ sudo ./confluent-hub install confluentinc/kafka-connect-elasticsearch:latest --component-dir /home/ec2-user --worker-configs /home/ec2-user/kafka-3.2.0-src/config/connect-distributed.properties
```
- --component-dir : 플러그인을 설치할 설치경로
- --worker-configs : connect-distributed.properties 파일이 있는 경로

<br>

# TIL-23.09.08

### 요구사항 명세서
![img.png](img.png)

### API 명세서
![img_1.png](img_1.png)
![img_3.png](img_3.png)

### 간트 차트
![img_4.png](img_4.png)


# TIL-23.09.11
### EC2 환경에서 kafka 클러스터 구축 및 kafka connect 설정
> 도커 컴포즈를 통해 3개의 브로커로 클러스터 실행
 ```dockerfile
version: '3.8'
services:
zookeeper-1:
image: confluentinc/cp-zookeeper:5.5.1
ports:
- '32181:32181'
environment:
ZOOKEEPER_CLIENT_PORT: 32181
ZOOKEEPER_TICK_TIME: 2000


kafka-1:
image: confluentinc/cp-kafka:5.5.1
ports:
- '9092:9092'
depends_on:
- zookeeper-1
environment:
KAFKA_BROKER_ID: 1
KAFKA_ZOOKEEPER_CONNECT: zookeeper-1:32181
KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT
KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka-1:29092,EXTERNAL://localhost:9092
KAFKA_DEFAULT_REPLICATION_FACTOR: 3
KAFKA_NUM_PARTITIONS: 3


kafka-2:
image: confluentinc/cp-kafka:5.5.1
ports:
- '9093:9093'
depends_on:
- zookeeper-1
environment:
KAFKA_BROKER_ID: 2
KAFKA_ZOOKEEPER_CONNECT: zookeeper-1:32181
KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT
KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka-2:29093,EXTERNAL://localhost:9093
KAFKA_DEFAULT_REPLICATION_FACTOR: 3
KAFKA_NUM_PARTITIONS: 3


kafka-3:
image: confluentinc/cp-kafka:5.5.1
ports:
- '9094:9094'
depends_on:
- zookeeper-1
environment:
KAFKA_BROKER_ID: 3
KAFKA_ZOOKEEPER_CONNECT: zookeeper-1:32181
KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT
KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka-3:29094,EXTERNAL://localhost:9094
KAFKA_DEFAULT_REPLICATION_FACTOR: 3
KAFKA_NUM_PARTITIONS: 3

# connect
 connect:
  image: confluentinc/cp-kafka-connect:7.2.0
    depends_on:
        - kafka1
        - kafka2
        - kafka3
  volumes:
    - /home/ec2-user/confluent-hub/plugins/confluentinc-kafka-connect-elasticsearch:/usr/share/java/kafka-connect-elasticsearch
  environment:
    CONNECT_PLUGIN_PATH: "/usr/share/java,/usr/share/java/kafka-connect-elasticsearch"
    CONNECT_CONFIG_STORAGE_TOPIC: "my_connect_configs"
    CONNECT_OFFSET_STORAGE_TOPIC: "my_connect_offsets"
    CONNECT_STATUS_STORAGE_TOPIC: "my_connect_statuses"
    CONNECT_KEY_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
    CONNECT_VALUE_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
    CONNECT_BOOTSTRAP_SERVERS: kafka1:9092,kafka2:9093,kafka3:9094
    CONNECT_REST_PORT: 8083
    CONNECT_REST_ADVERTISED_HOST_NAME: "15.165.116.126"
    CONNECT_GROUP_ID: compose-connect-group
```

kafka connect가 kafka 브로커에 접근하지 못하는 에러 발생..

# TIL-23.09.12

### kafka-Elasticsearch 파이프라인 구축

```dockerfile
 kafka-connect:
    image: confluentinc/cp-kafka-connect:5.5.1
    depends_on:
      - kafka-1
      - kafka-2
      - kafka-3
    ports:
      - "8083:8083"
    environment:
      CONNECT_PLUGIN_PATH: "/usr/share/java,/usr/share/java/kafka-connect-elasticsearch"
      CONNECT_CONFIG_STORAGE_TOPIC: "my_connect_configs"
      CONNECT_OFFSET_STORAGE_TOPIC: "my_connect_offsets"
      CONNECT_STATUS_STORAGE_TOPIC: "my_connect_statuses"
      CONNECT_KEY_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
      CONNECT_VALUE_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
      CONNECT_BOOTSTRAP_SERVERS: kafka-1:29092,kafka-2:29093,kafka-3:29094 #포트번호 주의!
      CONNECT_REST_PORT: 8083
      CONNECT_REST_ADVERTISED_HOST_NAME: "localhost"
      CONNECT_GROUP_ID: compose-connect-group
```

```dockerfile
# REST API로 엘라스틱서치 커넥터 등록
curl -X POST http://localhost:8083/connectors -H "Content-Type: application/json" -d '{
    "name": "elasticsearch-sink",
    "config": {
        "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
        "tasks.max": "1",
        # coupang 토픽을 감시
        "topics": "coupang",
        "key.ignore": "true",
        "schema.ignore": "true",
        # 엘라스틱서치 URL
        "connection.url": "http://13.125.74.24:9200",
        "type.name": "kafka-connect",
        "value.converter": "org.apache.kafka.connect.json.JsonConverter",
        "value.converter.schemas.enable": "false",
        "key.converter": "org.apache.kafka.connect.json.JsonConverter",
        "key.converter.schemas.enable": "false"
    }
}'
```

![img_5.png](img_5.png)
kafka-ui에서 kafka-connect 컨슈머 작동 확인

![img_6.png](img_6.png)
kibana에서 elasticsearch 모니터링하여 데이터 들어온걸 확인

# TIL-23.09.14

### 데이터 분산처리 파이프라인 완료

### 0. 고민

>1. 카테고리별 elasticsearch의 인덱스로 분리해서 저장하기
    초기에는 kafka connect를 이용해서 kafka-elasticsearch 파이프라인을  구축했기 때문에 SMT를 이용해서 필드의 값을 인덱스로 설정하려고했다.<br>
    하지만 아래 문제의 parent-child관계는 SMT방식으로 한계가 있다고 판단해서
    kafka - logstash - elasticsearch 로 파이프라인을 변경했다.
    output과정에서 index_name필드를 그대로 인덱스로 지정하면 되기 때문에 훨씬 간단하게 해결가능했다.

>2. 상품 상세 조회시 가격 추이 그래프를 제공하기 위해서 제목, 링크 등의 기본 정보와 시간별 가격 정보를 분리해서 저장시켜야 했다.
    함께 저장할 시 최대 5분마다 업데이트가 진행되는데 가격외에 고정적인 정보까지 같이 접근되기 때문에 따로 저장하기로 했다.<br>
    그래서 찾은 방법은 기본 정보와 가격 정보를 **parent-child관계**로 엘라스틱서치에 저장하는것
    join_field를 사용해서
```
{
  "mappings": {
    "properties": {
      "join_field": { 
        "type": "join",
        "relations": {
          "product": "price"  
        }
      }
    }
  }
}
```
elasticsearch에 이렇게 매핑시켜주게 되면 product를 부모로, price를 자식으로 인식하게 된다.
<br>

### 1. 크롤링

```python
import requests
import re
from bs4 import BeautifulSoup
import json
from kafka import KafkaProducer
from datetime import datetime
import time


def on_send_success(record_metadata):
    print(f"Record sent to topic {record_metadata.topic} partition [{record_metadata.partition}] offset [{record_metadata.offset}]")

def on_send_error(excp):
    print(f"I am an errback: {excp}")

# Kafka 설정
producer = KafkaProducer(
    bootstrap_servers=['{ip주소}:9092'],
    value_serializer=lambda v: json.dumps(v).encode('utf-8'), # json으로 파싱
    key_serializer=lambda v: json.dumps(v).encode('utf-8')
)

HEADERS = {
    "User-Agent": "...",
    "Accept-Language": "ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3"
}

SEARCH_PRODUCT_CLASS = re.compile("^search-product")

# 페이지 범위 설정
page_range = range(1, 4)

id = 200
for i in page_range:
    url = f"https://www.coupang.com/np/search?component=&q=%EC%95%84%EC%9D%B4%ED%8C%A8%EB%93%9C+%EC%97%90%EC%96%B4+5&channel=auto"

    try:
        res = requests.get(url, headers=HEADERS)
        res.raise_for_status()
    except requests.RequestException as e:
        print(f"Request failed: {e}")
        continue

    soup = BeautifulSoup(res.text, "html.parser")
    items = soup.find_all("li", attrs={"class": SEARCH_PRODUCT_CLASS})

    current_time = datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S')

    for item in items:
        ad_badge = item.find("span", attrs={"class": "ad-badge-text"})
        if ad_badge:
            continue

        original_name = item.find("div", attrs={"class": "name"}).get_text()
        name = original_name.strip()

        price_tag = item.find("strong", attrs={"class": "price-value"})
        if price_tag:
            price = price_tag.get_text()
        else:
            print("가격 정보를 찾을 수 없습니다.")
            continue

        rate = item.find("em", attrs={"class": "rating"})
        if rate:
            rate = rate.get_text()
        else:
            continue

        rate_cnt = item.find("span", attrs={"class": "rating-total-count"})
        if rate_cnt:
            rate_cnt = rate_cnt.get_text()[1:-1]
        else:
            continue

        link = item.find("a", attrs={"class": "search-product-link"})["href"]

        if float(rate) >= 3 and int(rate_cnt) >= 10:
                print(f"제품명 : {name}")

                #Product 메시지
                product_message = {
                    "id": f"product_{id}",
                    "major_category": "가전",
                    "minor_category": "노트북",
                    "product_name": name,
                    "rate": rate,
                    "index_name": "테스트2",
                    "product_link": f"https://www.coupang.com/{link}",
                    "product_image": "http://example.com/images/lg-gram.jpg"
                    }

                producer.send('aaa', key=f"product_{id}", value=product_message).add_callback(on_send_success).add_errback(on_send_error)


                # Price 메시지
                price_message = {
                    "id": f"price_{id}",
                    "routing": f"product_{id}",
                    "index_name": "테스트2",
                    "timestamp": current_time,
                    "price": price
                    }

                #producer.send('electronic', key=f"product_{id}".encode('utf-8'), value=json.dumps(product_message).encode('utf-8')).add_callback(on_send_success).add_errback(on_send_error)
                producer.send('aaa', key=f"price_{id}", value=price_message).add_callback(on_send_success).add_errback(on_send_error)
                id += 1

# Kafka Producer를 flush하여 모든 메시지가 전송
producer.flush()
```

크롤링한 뒤 기본 정보와 가격 정보를 따로 kafka로 전송
<br>

### 2. Kafka

kafka 클러스터는 이전에 이미 구축해놓았기 때문에 자세한 내용은 생략
3개의 브로커로 구성했고, 도커 컴포즈로 실행했다
```dockerfile
---
version: '3.8'
services:
  zookeeper-1:
    image: confluentinc/cp-zookeeper:5.5.1
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    volumes:
      - /var/lib/zookeeper/data:/var/lib/zookeeper/data
      - /var/lib/zookeeper/log:/var/lib/zookeeper/log


  kafka-1:
    image: confluentinc/cp-kafka:5.5.1
    ports:
      - '9092:9092'
    extra_hosts:
      - "localhost:0.0.0.0"
    depends_on:
      - zookeeper-1
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper-1:2181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
      KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka-1:29092,EXTERNAL://{ip주소}:9092
      KAFKA_DEFAULT_REPLICATION_FACTOR: 3
      KAFKA_NUM_PARTITIONS: 3
      
  kafka-2:
    image: confluentinc/cp-kafka:5.5.1
    ports:
      - '9093:9093'
    extra_hosts:
      - "localhost:0.0.0.0"
    depends_on:
      - zookeeper-1
    environment:
      KAFKA_BROKER_ID: 2
      KAFKA_ZOOKEEPER_CONNECT: zookeeper-1:2181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
      KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka-2:29093,EXTERNAL://{ip주소}:9093
      KAFKA_DEFAULT_REPLICATION_FACTOR: 3
      KAFKA_NUM_PARTITIONS: 3


  kafka-3:
    image: confluentinc/cp-kafka:5.5.1
    ports:
      - '9094:9094'
    extra_hosts:
      - "localhost:0.0.0.0"
    depends_on:
      - zookeeper-1
    environment:
      KAFKA_BROKER_ID: 3
      KAFKA_ZOOKEEPER_CONNECT: zookeeper-1:2181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
      KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka-3:29094,EXTERNAL://{ip주소}:9094
      KAFKA_DEFAULT_REPLICATION_FACTOR: 3
      KAFKA_NUM_PARTITIONS: 3
```
![](https://velog.velcdn.com/images/ytytyt0427/post/37879bd3-f8f2-4129-b614-a38626ee48da/image.png)
kafka-ui를 통해 모니터링

### 2-1. kafka topic관리
```
docker exec -it {컨테이너명} /bin/bash
# topic 삭제
kafka@84beaf5e1414:/# kafka-topics --bootstrap-server localhost:9092 --delete --topic {삭제할 토픽명}
# consumer 삭제
kafka@84beaf5e1414:/# kafka-consumer-groups --bootstrap-server localhost:9092 --delete --group {삭제할 consumer}
```

### 2-2. kafka 메시지
- product 메시지
  ![](https://velog.velcdn.com/images/ytytyt0427/post/e355d785-1554-4b34-8970-19c2898b9eae/image.png)
- price 메시지
  ![](https://velog.velcdn.com/images/ytytyt0427/post/e1b90429-d3f6-40cd-91e0-e55d9c312e63/image.png)

<br>

### 3. Logstash



```
# logstash.conf
input {
  kafka {
    bootstrap_servers => "{kafka ip주소}:9092"
    topics => ["{감시할 topic명}"]
    group_id => "logstash" # consumer 이름 지정
    consumer_threads => 3 
  }
}

filter {
  # elasticsearch에서 인식하기 위해 json으로 변환
  json {
    source => "message"
  }
    # parent id 추출
  if [id] =~ /^price_/ {
    grok {
      match => { "id" => "price_(?<actual_id>.*)"}
    }
    mutate {
      add_field => { "[join_field][parent]" => "product_%{actual_id}" }
    }
  }

  # join_field 설정
  if [id] =~ /^product_/ {
    mutate {
      add_field => { "[join_field][name]" => "product" }
    }
  } else if [id] =~ /^price_/ {
    mutate {
      add_field => { "[join_field][name]" => "price" }
    }
  }
}

output {
  # routing 설정
  if [join_field][name] == "price" {
    elasticsearch {
      hosts => ["{elasticsearch ip주소}"]
      # 인덱스 필드를 elastcsearch의 인덱스이름으로 사용
      index => "%{[index_name]}"
      # id 필드를 도큐먼트 id로 사용
      document_id => "%{[id]}"
      # 같은 샤드로 담기 위해 routing 설정
      routing => "%{[routing]}"
    }
  } else {
    elasticsearch {
      hosts => ["{elasticsearch ip 주소}"]
            index => "%{[index_name]}"
      document_id => "%{[id]}"
    }
  }
}
                                                     
```

도커 컴포즈 파일의 logstash부분에 설정파일 볼륨 추가
```
  logstash:
    image: docker.elastic.co/logstash/logstash:7.14.0
    ports:
      - 5044:5044
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
      - "LS_JAVA_OPTS=-Xmx512m -Xms512m"
    networks:
      - esnet
    depends_on:
      - elasticsearch
      - elasticsearch2
      - elasticsearch3
    volumes:
      - logstash-data:/usr/share/logstash/data
      - /home/ubuntu/ELK/logstash/logstash.conf:/usr/share/logstash/pipeline/logstash.conf # 추가 내용
```


- join_field에서 child의 경우 parent를 지정해야 하는데, grok으로 price메시지의 숫자부분을 actual_id에 저장해놓고 필드추가할때 사용
- parent의 경우 join_field에 name 추가, child의 경우 name, parent추가

=> 지정한 kafka topic에서 메시지를 가져와서 변환 후 elasticsearch로 전송

### 4. Elasticsearch
> elasticsearch에 데이터를 넣기 전 join mapping 설정을 해놓아야한다.
![](https://velog.velcdn.com/images/ytytyt0427/post/bbb869c4-0e0c-40e6-90de-58f88e3af134/image.png)
kibana로 접속 후 Dev tools에서 REST API 형태로 요청 가능
요청하면 해당 인덱스가 자동으로 생성되는것을 stack management - index management에서 확인 가능

> Index Patterns에서 인덱스를 추가해준뒤 Discover로 이동해서 데이터 삽입 확인
![](https://velog.velcdn.com/images/ytytyt0427/post/c3568522-77f8-45b0-a4c0-48cf52e0a5f7/image.png)

> Dev tools에서 join 관계 잘 설정 되었는지 확인해보자
![](https://velog.velcdn.com/images/ytytyt0427/post/d910ddce-e746-43b4-a1f4-d3b8c9dbddc9/image.png)

### 4-1. 가격 업데이트
```
output {
  elasticsearch {
    hosts => ["http://localhost:9200"]
    index => "테스트"
    document_id => "%{[id]}"  # Kafka 메시지에서 id 필드를 가져옴
    action => "update"
    doc_as_upsert => true  # 해당 문서가 없으면 새로 생성
    script => "ctx._source.timestamp = params.timestamp; ctx._source.price = params.price;"
    script_params => { "timestamp" => "%{[timestamp]}", "price" => "%{[price]}" }
  }
}

```
추후 위의 코드를 logstash.conf에 추가하여 가격 정보를 추가할 수 있다.



