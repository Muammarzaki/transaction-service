# APPLICATION SPECIFICATION

## Endpoint

- /transact
    - /
    - /check?id=id
    - /cancel?id=id
    - /list_all

## Response Body

[GET] **/transact/check?id=id**

Response

```json
{
  "status_code": 200,
  "status": "http.ok",
  "data": {
    "transact_id": "id",
    "payment_id": "id_payment",
    "transact_status": "done|pending|fail|cancel",
    "mount": 40000,
    "currency": "IDR",
    "transact_on": "timestamp",
    "transact_finish_on": "fill if transact_status is done",
    "transact_method": "gopay",
    "items": [
      {
        "item_id": "random",
        "item_name": "megicom",
        "count": 2,
        "price": 20000
      }
    ],
    "costumer": {
      "user_id": "random",
      "username": "joni"
    }
  }
}
```

[POST] **/transact/cancel?id=id**

Response

```json
{
  "status_code": 202,
  "status": "http.accepted",
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
  "status_code": 200,
  "status": "http.ok",
  "data": {
    "transact_id": "id",
    "payment_id": "id_payment",
    "transact_status": "done|pending|fail|cancel",
    "mount": 40000,
    "currency": "IDR",
    "transact_on": "timestamp",
    "transact_finish_on": "fill if transact_status not pending",
    "transact_method": "gopay",
    "items": [
      {
        "item_id": "random",
        "item_name": "megicom",
        "count": 2,
        "price": 20000
      }
    ],
    "costumer": {
      "user_id": "random",
      "username": "joni"
    }
  }
}
```

[POST] /transact

Request

```json
{
  "mount": 40000,
  "currency": "IDR",
  "transact_method": "gopay",
  "items": [
    {
      "item_id": "random",
      "item_name": "megicom",
      "count": 2,
      "price": 20000
    }
  ],
  "costumer": {
    "user_id": "random",
    "username": "joni"
  }
}

```

Response

```json
{
  "status_code": 200,
  "status": "http.ok",
  "data": {
    "transact_id": "id",
    "payment_id": "id_payment",
    "transact_status": "done|pending|fail|cancel",
    "mount": 40000,
    "currency": "IDR",
    "transact_on": "timestamp",
    "transact_finish_on": "fill if transact_status is done",
    "transact_method": "gopay",
    "items": [
      {
        "item_id": "random",
        "item_name": "megicom",
        "count": 2,
        "price": 20000
      }
    ],
    "costumer": {
      "user_id": "random",
      "username": "joni"
    }
  }
}
```

## Each header

Request

> REQ_TOKEN = base64(salt + username + salt)

Response

> RES_TOKEN = base64(salt + username + salt)