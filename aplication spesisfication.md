# APPLICATION SPECIFICATION

## Endpoint

- /api
    - /auth
        - /register
    - /transact
        - /create
        - /check?id=id
        - /cancel?id=id
        - /list_all

## Structure Request and Response body

[POST] /auth/register

Request

```json
{
  "username": "user",
  "password": "base64(password)"
}
```

Response


[GET] **/transact/check?id=id**

Response

```json
{
  "data": {
    "order_id": "id_payment",
    "transact_status": "done|pending|fail|cancel",
    "gross_mount": 40000,
    "currency": "IDR",
    "transact_on": "timestamp",
    "transact_finish_on": "fill if transact_status is done",
    "transact_method": "gopay",
    "items": [
      {
        "item_id": "random",
        "item_name": "comedic",
        "quantity": 2,
        "price": 20000
      }
    ],
    "customer": {
      "user_id": "random",
      "first_name": "joni",
      "last_name": "",
      "email": "cp@example.com",
      "phone": "62832242445332"
    },
    "expired": "date"
  }
}
```

[POST] **/transact/cancel?id=id**

Response

```json
{
  "data": {
    "transact_id": "id",
    "transact_status": "cancel",
    "mount": 4000,
    "currency": "IDR",
    "transact_on": "timestamp",
    "transact_finis_on": "timestamp_now",
    "transact_method": "gopay"
  }
}
```

[GET] **/transact/list_all**

Response

```json
{
  "data": {
    "order_id": "id_payment",
    "transact_status": "done|pending|fail|cancel",
    "mount": 40000,
    "currency": "IDR",
    "transact_on": "timestamp",
    "transact_finish_on": "fill if transact_status not pending",
    "transact_method": "gopay",
    "items": [
      {
        "item_id": "random",
        "item_name": "comedic",
        "quantity": 2,
        "price": 20000
      }
    ],
    "customer": {
      "user_id": "random",
      "first_name": "joni",
      "last_name": "",
      "email": "cp@example.com",
      "phone": "62832242445332"
    },
    "expired": "date"
  }
}
```

[POST] /transact/create

Request

```json
{
  "mount": 40000,
  "currency": "IDR",
  "transact_method": "gopay",
  "items": [
    {
      "item_id": "random",
      "item_name": "comedic",
      "quantity": 2,
      "price": 20000
    }
  ],
  "customer": {
    "user_id": "random",
    "first_name": "joni",
    "last_name": "",
    "email": "cp@example.com",
    "phone": "62832242445332"
  }
}

```

Response

```json
{
  "data": {
    "order_id": "id_payment",
    "transact_status": "done|pending|fail|cancel",
    "mount": 40000,
    "currency": "IDR",
    "transact_on": "timestamp",
    "transact_finish_on": "fill if transact_status is done",
    "transact_method": "gopay",
    "items": [
      {
        "item_id": "random",
        "item_name": "comedic",
        "quantity": 2,
        "price": 20000
      }
    ],
    "customer": {
      "user_id": "random",
      "first_name": "joni",
      "last_name": "",
      "email": "cp@example.com",
      "phone": "62832242445332"
    },
    "expired": "date"
  }
}
```

## Each header

Request

> TOKEN = base64(salt + username + salt)

Response

> TOKEN = base64(salt + username + salt)

## Entity Structure

**Table transact**

| entity            | type     | other    |
|-------------------|----------|----------|
| id                | Long     | primary  |
| transact_id       | String   |          |
| order_id          | String   |          |
| mount             | int      |          |
| currency          | Currency |          |
| transact_on       | time     |          |
| transact_finis_on | time     |          |
| transact_method   | String   |          |
| items             | items    | relation |
| customer_info     | customer | relation |

**Table item**

| entity  | type   | other   |
|---------|--------|---------|
| id      | Long   | primary |
| item_id | String |         |
| price   | int    |         |
| count   | int    |         |

**Table customer_info**

| entity   | type   | other   |
|----------|--------|---------|
| id       | Long   | primary |
| user_id  | String |         |
| username | String |         |

# third-party API

## Midtrans Payment Gateway

### Docs

|                   |                                        |
|-------------------|----------------------------------------|
| Documentation     | https://docs.midtrans.com/docs         |
| API References    | https://docs.midtrans.com/reference    |
| SandBox Simulator | https://simulator.sandbox.midtrans.com |

### Core API

#### URL

| mode       | url                                 |
|------------|-------------------------------------|
| Sandbox    | https://api.sandbox.midtrans.com/v2 |
| Production | https://api.midtrans.com/v2         |  

#### Endpoint

| method | endpoint           | description                                                                                                                                                                                                                                            |
|--------|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| POST   | /charge            | for create new transaction                                                                                                                                                                                                                             |
| POST   | {order_id}/cancel  | for cancel The transaction                                                                                                                                                                                                                             |
| POST   | /capture           | Capture transaction is triggered to capture the transaction balance when transaction_status:authorize. This is only available after Pre-Authorized Credit Card or Pre-Authorized GoPay.                                                                |
| POST   | /{order_id}/refund | Refund transaction is called to reverse the money back to customers for transactions with payment status Settlement. If transaction's status is still Pending Authorize or Capture please use Cancel API instead. The same refund_id cannot be reused. |
| GET    | {order_id}/status  | Get Status the transaction                                                                                                                                                                                                                             |

#### HTTP Header

```text
Accept: application/json
Content-Type: application/json
Authorization: Basic AUTH_STRING
```

AUTH_STRING = BASE64(server_key + :)

#### Over the Counter Payment

| Support Method | Alfamart | Indomaret |
|----------------|----------|-----------|

##### Alfamart Example Request

```shell
curl -X POST \
  https://api.sandbox.midtrans.com/v2/charge \
  -H 'Accept: application/json' \
  -H 'Authorization: Basic <YOUR SERVER KEY ENCODED in Base64>' \
  -H 'Content-Type: application/json' \
  -d '{
  "payment_type": "cstore",
  "transaction_details": {
      "order_id": "order-101",
      "gross_amount": 44000
  }
  "cstore" : {
    "store" : "alfamart",
    "message" : "Message ",
    "alfamart_free_text_1": "1st row of receipt,",
    "alfamart_free_text_2": "This is the 2nd row,",
    "alfamart_free_text_3": "3rd row. The end."
  }
}'
```

##### Indomaret Example Request

```shell
curl -X POST \
  https://api.sandbox.midtrans.com/v2/charge \
  -H 'Accept: application/json' \
  -H 'Authorization: Basic <YOUR SERVER KEY ENCODED in Base64>' \
  -H 'Content-Type: application/json' \
  -d '{
  "payment_type": "cstore",
  "transaction_details": {
      "order_id": "order-101",
      "gross_amount": 44000
  },
  "cstore" : {
    "store" : "indomaret",
    "message" : "Message to display"
  }
}'
```

##### Alfamart Example Response

```json
{
  "status_code": "201",
  "status_message": "Success, cstore transaction is successful",
  "transaction_id": "d615df87-c96f-4f5c-9d35-2d740d54c1a9",
  "order_id": "order-101o-1578557780",
  "merchant_id": "G812785002",
  "gross_amount": "162500.00",
  "currency": "IDR",
  "payment_type": "cstore",
  "transaction_time": "2020-01-09 15:16:19",
  "transaction_status": "pending",
  "fraud_status": "accept",
  "payment_code": "8127740588870520",
  "store": "alfamart"
}
```

##### Indomaret Example Response

```json
{
  "status_code": "201",
  "status_message": "Success, cstore transaction is successful",
  "transaction_id": "9b3951a4-da50-4089-86df-161d3e9251df",
  "order_id": "order-101n-1578557719",
  "gross_amount": "44000.00",
  "currency": "IDR",
  "payment_type": "cstore",
  "transaction_time": "2020-01-09 15:15:19",
  "transaction_status": "pending",
  "merchant_id": "G812785002",
  "payment_code": "578112341234",
  "store": "indomaret"
}
```

#### Integration: E-Wallet

| Support Method : | Gopay | QRIS |
|------------------|-------|------|

Example Request

```shell
curl -X POST \
  https://api.sandbox.midtrans.com/v2/charge \
  -H 'Accept: application/json'\
  -H 'Authorization: Basic <YOUR SERVER KEY ENCODED in Base64>' \
  -H 'Content-Type: application/json' \
  -d '{
    "payment_type": "gopay",
    "transaction_details": {
        "order_id": "order-101",
        "gross_amount": 44000
    }
}'
```

Example Response

```json
{
  "status_code": "201",
  "status_message": "GO-PAY transaction is created",
  "transaction_id": "231c79c5-e39e-4993-86da-cadcaee56c1d",
  "order_id": "order-101h-1570513296",
  "gross_amount": "44000.00",
  "currency": "IDR",
  "payment_type": "gopay",
  "transaction_time": "2019-10-08 12:41:36",
  "transaction_status": "pending",
  "fraud_status": "accept",
  "actions": [
    {
      "name": "generate-qr-code",
      "method": "GET",
      "url": "https://api.sandbox.veritrans.co.id/v2/gopay/231c79c5-e39e-4993-86da-cadcaee56c1d/qr-code"
    },
    {
      "name": "deeplink-redirect",
      "method": "GET",
      "url": "https://simulator.sandbox.midtrans.com/gopay/ui/checkout?referenceid=Y0xwjoQ9uy&callback_url=someapps%3A%2F%2Fcallback%3Forder_id%3Dorder-101h-1570513296"
    },
    {
      "name": "get-status",
      "method": "GET",
      "url": "https://api.sandbox.veritrans.co.id/v2/231c79c5-e39e-4993-86da-cadcaee56c1d/status"
    },
    {
      "name": "cancel",
      "method": "POST",
      "url": "https://api.sandbox.veritrans.co.id/v2/231c79c5-e39e-4993-86da-cadcaee56c1d/cancel"
    }
  ]
}
```

#### Integration: Card less Credit Payment

| Support Method | Akulaku |
|----------------|---------|

**Example Request**

```shell
curl -X POST \
  https://api.sandbox.midtrans.com/v2/charge \
  -H 'Accept: application/json' \
  -H 'Authorization: Basic <YOUR SERVER KEY ENCODED in Base64>' \
  -H 'Content-Type: application/json' \
  -d '{
  "payment_type": "akulaku",
  "transaction_details": {
      "order_id": "order-101",
      "gross_amount": 44000
  }
}
```

**Example Response**

```json
{
  "status_code": "201",
  "status_message": "Success, Akulaku transaction is created",
  "transaction_id": "fa05cba0-8ea3-4e46-a2b1-daea2a01785c",
  "order_id": "order-101-1578567480",
  "redirect_url": "https://api.sandbox.midtrans.com/v2/akulaku/redirect/fa05cba0-8ea3-4e46-a2b1-daea2a01785c",
  "merchant_id": "G812785002",
  "gross_amount": "11000.00",
  "currency": "IDR",
  "payment_type": "akulaku",
  "transaction_time": "2020-01-09 17:58:00",
  "transaction_status": "pending",
  "fraud_status": "accept"
}
```

#### Integration: Bank Transfer

| Support Method | BCA | BRI | BNI | CIMB |
|----------------|-----|-----|-----|------|

**Example Request**

```shell
curl -X POST \
  https://api.sandbox.midtrans.com/v2/charge \
  -H 'Accept: application/json' \
  -H 'Authorization: Basic <YOUR SERVER KEY ENCODED in Base64>' \
  -H 'Content-Type: application/json' \
  -d '{
  "payment_type": "bank_transfer",
  "transaction_details": {
      "order_id": "order-101",
      "gross_amount": 44000
  },
  "bank_transfer":{
      "bank": "bca"
  }
}'
```

**Example Response**

BCA

```json
{
  "status_code": "201",
  "status_message": "Success, Bank Transfer transaction is created",
  "transaction_id": "be03df7d-2f97-4c8c-a53c-8959f1b67295",
  "order_id": "1571823229",
  "merchant_id": "G812785002",
  "gross_amount": "44000.00",
  "currency": "IDR",
  "payment_type": "bank_transfer",
  "transaction_time": "2019-10-23 16:33:49",
  "transaction_status": "pending",
  "va_numbers": [
    {
      "bank": "bca",
      "va_number": "812785002530231"
    }
  ],
  "fraud_status": "accept"
}
```

BNI

```json
{
  "status_code": "201",
  "status_message": "Success, Bank Transfer transaction is created",
  "transaction_id": "2194a77c-a412-4fd8-8ec8-121ff64fbfee",
  "order_id": "1571823369",
  "merchant_id": "G812785002",
  "gross_amount": "44000.00",
  "currency": "IDR",
  "payment_type": "bank_transfer",
  "transaction_time": "2019-10-23 16:36:08",
  "transaction_status": "pending",
  "va_numbers": [
    {
      "bank": "bni",
      "va_number": "9888500212345678"
    }
  ],
  "fraud_status": "accept"
}
```

BRI

```json
{
  "status_code": "201",
  "status_message": "Success, Bank Transfer transaction is created",
  "transaction_id": "9aed5972-5b6a-401e-894b-a32c91ed1a3a",
  "order_id": "1466323342",
  "gross_amount": "20000.00",
  "payment_type": "bank_transfer",
  "transaction_time": "2016-06-19 15:02:22",
  "transaction_status": "pending",
  "va_numbers": [
    {
      "bank": "bri",
      "va_number": "8578000000111111"
    }
  ],
  "fraud_status": "accept",
  "currency": "IDR"
}
```

CIMB

```json
{
  "status_code": "201",
  "status_message": "Success, Bank Transfer transaction is created",
  "transaction_id": "2194a77c-a412-4fd8-8ec8-121ff64fbfee",
  "order_id": "1571823369",
  "merchant_id": "G812785002",
  "gross_amount": "44000.00",
  "currency": "IDR",
  "payment_type": "bank_transfer",
  "transaction_time": "2022-10-23 16:36:08",
  "transaction_status": "pending",
  "va_numbers": [
    {
      "bank": "cimb",
      "va_number": "2810490150230740"
    }
  ],
  "expiry_time": "2023-06-29 15:15:58"
}
```

#### Integration: Card Payment

Credit Token get from https://docs.midtrans.com/docs/coreapi-card-payment-integration

**Example Request**

```shell
curl -X POST \
  https://api.sandbox.midtrans.com/v2/charge \
  -H 'Accept: application/json'\
  -H 'Authorization: Basic <YOUR SERVER KEY ENCODED in Base64>' \
  -H 'Content-Type: application/json' \
  -d '{
  	"payment_type": "credit_card",
  	"transaction_details": {
    	"order_id": "order102",
    	"gross_amount": 789000
  	},
  	"credit_card": {
    	"token_id": "<token_id from Get Card Token Step>",
    	"authentication": true,
  	}
    "customer_details": {
        "first_name": "bud",
        "last_name": "catamaran",
        "email": "budi.pra@example.com",
        "phone": "08111222333"
    }
}'
```

**Example Response**

```json
{
  "status_code": "201",
  "status_message": "Success, Credit Card transaction is successful",
  "transaction_id": "0bb563a9-ebea-41f7-ae9f-d99ec5f9700a",
  "order_id": "order102",
  "redirect_url": "https://api.sandbox.veritrans.co.id/v2/token/rba/redirect/48111111-1114-0bb563a9-ebea-41f7-ae9f-d99ec5f9700a",
  "gross_amount": "789000.00",
  "currency": "IDR",
  "payment_type": "credit_card",
  "transaction_time": "2019-08-27 15:50:54",
  "transaction_status": "pending",
  "fraud_status": "accept",
  "masked_card": "48111111-1114",
  "bank": "bni",
  "card_type": "credit",
  "three_ds_version": "2",
  "challenge_completion": true
}
```

#### Important Request Attribute

```json
{
  "payment_type": "credit_card",
  "transaction_details": {
    "gross_amount": 789000,
    "order_id": "order102"
  }
}
```

and other is base on payment_type. somehow we need to share customer information and items, we can share it with
attribute customer_details and items\

```json
{
  "customer_details": {
    "first_name": "bud",
    "last_name": "catamaran",
    "email": "budi.pra@example.com",
    "phone": "08111222333"
  },
  "items": [
    {
      "item_id": "random",
      "item_name": "comedic",
      "count": 2,
      "price": 20000
    }
  ]
}
```

#### Important Response Attribute

##### Success

```json
{
  "status_code": 200,
  "status_message": "GO-PAY transaction is created",
  "currency": "IDR",
  "fraud_status": "accept",
  "gross_amount": "162500.00",
  "order_id": "order-101o-1578557780",
  "payment_type": "cstore",
  "transaction_id": "d615df87-c96f-4f5c-9d35-2d740d54c1a9",
  "transaction_status": "pending",
  "transaction_time": "2020-01-09 15:16:19"
}
```

#### Fail

```json
{
  "status_code": 433,
  "status_message": "foobar"
}
```