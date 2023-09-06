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


# TIL-23.09.06

### 데이터 분산 프로세스 구축

> 1. 쿠팡, 11번가에서 크롤링 및 API를 통해 상품 데이터 수집
> 2. 수집된 데이터는 kafka producer 역할을 하며 카테고리를 토픽으로 생성
> 3. kafka 클러스터는 docker-compose를 통해 3개의 브로커로 구축
> 4. kafka connect로 실시간 업데이트되는 컨텐츠를 감시하고 logstash로 전송
> 5. logstash에서 데이터 포맷 통일 후 Elasticsearch로 전송
> 6. ElasticSearch에서 데이터 저장 및 관리

