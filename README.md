# Currency Exchange API

REST API for monitoring currencies, exchange rates and performing exchange
between currencies.

## Technology stack

- Java 17
- JDBC, SQLite
- Jakarta Servlets API
- Apache Maven
- Apache Tomcat
- Jackson

## How to run

Make sure you have Java 17, Apache Tomcat 11 and Apache Maven installed. 

Also set Tomcat's installed directory in environment variable `CATALINA_BASE`.
1. Clone repository

    ```shell
    git clone https://github.com/bychenkv/currency-exchange.git
    cd currency-exchange
    ```

2. Package WAR

    ```shell
    mvn clean package
    ```

3. Copy WAR into Tomcat's `webapps` directory

    ```shell
    mv target/currency-exchange.war $CATALINA_BASE/webapps
    ```

4. Set database path in environment variable `CURRENCY_DB_PATH`

    ```shell
    export CURRENCY_DB_PATH="/path/to/your/currency.db"
    ```
   
    By default, database path is `${user.home}/currency.db`.


5. Run Tomcat

    ```shell 
   $CATALINA_BASE/bin/startup.sh
    ```

6. API is now available at `http://localhost:8080/currency-exchange`
## API Endpoints

### Currencies

- `GET /currencies` – getting a list of all currencies

    #### Response example

    ```json
    [
        {
            "id": 0,
            "name": "United States dollar",
            "code": "USD",
            "sign": "$"
        },   
        {
            "id": 0,
            "name": "Euro",
            "code": "EUR",
            "sign": "€"
        }
    ]
    ```
  
    #### HTTP response codes

    - Success - 200
    - Error (e.g., database is unavailable) - 500

    
- `GET /currency/{code}` – getting a currency by ISO code

  #### Response example

    ```json
    {
        "id": 0,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    }
    ```

  #### HTTP response codes

  - Success - 200
  - Currency code is missing - 400
  - Currency not found - 404
  - Error (e.g., database is unavailable) - 500


- `POST /currencies` – adding a new currency

    #### Request
    
    Use `application/x-www-form-urlencoded` encoding. Fields: `code`, `name`, `sign`.

    #### Response example

    ```json
      {
        "id": 0,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    }
    ```
  
    #### HTTP response codes

    * Success - 201
    * Required form field is missing - 400
    * Currency already exists - 409
    * Error (e.g., database is unavailable) - 500
    
    
### Exchange rates

- `GET /exchangeRates` – getting a list of all exchange rates

    #### Response example
    
    ```json
     [
        {
            "id": 0,
            "baseCurrency": {
                "id": 0,
                "name": "United States dollar",
                "code": "USD",
                "sign": "$"
            },
            "targetCurrency": {
                "id": 1,
                "name": "Euro",
                "code": "EUR",
                "sign": "€"
            },
            "rate": 0.99
        }
    ]
    ```
  
    #### HTTP response codes

    - Success - 200
    - Error (e.g., database is unavailable) - 500


- `GET /exchangeRate/{baseCode}{targetCode}` – getting exchange rate for currency code pair

  #### Response example

    ```json
    {
        "id": 0,
        "baseCurrency": {
            "id": 0,
            "name": "United States dollar",
            "code": "USD",
            "sign": "$"
        },
        "targetCurrency": {
            "id": 2,
            "name": "Russian Ruble",
            "code": "RUB",
            "sign": "₽"
        },
        "rate": 80
    }
    ```

  #### HTTP response codes

    - Success - 200
    - Currency code pair is missing - 400
    - Exchange rate not found - 404
    - Error (e.g., database is unavailable) - 500


* `POST /exchangeRates` – adding a new exchange rate

    #### Request

    Use `application/x-www-form-urlencoded` encoding. Fields: `baseCurrencyCode`, `targetCurrencyCode`, `rate`.

    #### Response example

    ```json
    {
        "id": 0,
        "baseCurrency": {
            "id": 0,
            "name": "United States dollar",
            "code": "USD",
            "sign": "$"
        },
        "targetCurrency": {
            "id": 1,
            "name": "Euro",
            "code": "EUR",
            "sign": "€"
        },
        "rate": 0.99
    }
    ```
  
    #### HTTP response codes

    - Success - 201 
    - Required form field is missing - 400
    - Currency pair already exists - 409
    - One (or both) currencies don't exist - 404
    - Error (e.g., database is unavailable) - 500


- `PATCH /exchangeRate/{baseCode}{targetCode}` – update exchange rate for currency code pair

    #### Request
    
    Use `application/x-www-form-urlencoded` encoding. Only one field – `rate`.

    #### Response example

    ```json
    {
        "id": 0,
        "baseCurrency": {
            "id": 0,
            "name": "United States dollar",
            "code": "USD",
            "sign": "$"
        },
        "targetCurrency": {
            "id": 2,
            "name": "Russian Ruble",
            "code": "RUB",
            "sign": "₽"
        },
        "rate": 80
    }
    ```
  
    #### HTTP response codes

  - Success - 200
  - Required form field is missing - 400
  - Currency pair not found in database - 404
  - Error (e.g., database is unavailable) - 500


### Exchange

Exchange can be performed by direct, reversed and cross (using USD) rates

- `GET /exchange?from=BASE&to=TARGET&amount=AMOUNT` – calculating conversion of a certain amount

    #### Response example

    ```json
    {
        "baseCurrency": {
            "id": 0,
            "name": "United States dollar",
            "code": "USD",
            "sign": "$"
        },
        "targetCurrency": {
            "id": 1,
            "name": "Australian dollar",
            "code": "AUD",
            "sign": "A$"
        },
        "rate": 1.45,
        "amount": 10.00,
        "convertedAmount": 14.50
    }
    ```

    #### HTTP response codes

    - Success - 200
    - Required query parameter is missing or invalid - 400
    - Base and/or target currency aren't found in database - 404
    - Exchange can't be performed by any of the methods - 404
    - Error (e.g., database is unavailable) - 500