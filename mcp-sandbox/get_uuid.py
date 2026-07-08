import requests
import sys

# requests 라이브러리가 설치되어 있는지 확인
try:
    response = requests.get('https://httpbin.org/uuid')
    print(response.json()['uuid'])
except Exception as e:
    print(f"Error: {e}")
