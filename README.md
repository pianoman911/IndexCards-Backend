# IndexCards-Backend
[![GPLv3 License](https://img.shields.io/badge/License-GPL%20v3-yellow.svg)](https://opensource.org/licenses/)

This is the backend for the poorly coded IndexCards app. But it works :)



## Demo

https://indexcards.finndohrmann.de/


## FAQ

#### Why is this project poorly coded?

The project simply consists of a few tests. The security design should not be adopted in any case. Also, this is my first web API project and it should be fast and efficient.

#### For whom is the project intended?

It's a simple school project.


## Run Locally

Clone the project

```bash
  git clone https://github.com/pianoman911/IndexCards-Backend.git
```

Go to the project directory

```bash
  cd indexcards-backend
```

Build the jar

```bash
  ./gradlew build
```

Start the server

```bash
  java -jar build/libs/IndexCards-1.0.0-all.jar
```


## API Reference

### Create an account
#### Request

```http
  POST /api/account/create
```

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `name` | `string` | **Required**. Account Name |
| `password` | `string` | **Required**. Password. Usefully hashed. |

#### Respone
| Code  | Meaning                                      |
|:------|:---------------------------------------------| 
| `201` | `Account created`                            |
| `400` | `An Error occured while creating an account` |

| Parameter | Type | Description |
|:----------|:-----|:------------|
| `-`       | `-`  | -           |

### Auth an account
#### Request

```http
  POST /api/account/auth
```

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `name` | `string` | **Required**. Account Name |
| `password` | `string` | **Required**. Password. Usefully hashed. |

#### Respone
| Code  | Meaning                  |
|:------|:-------------------------| 
| `200` | `Sucessfully authorized` |
| `400` | `Fields empty`           |
| `401` | `No valid credentials`   |

| Parameter | Type     | Description                                                               |
|:----------|:---------|:--------------------------------------------------------------------------|
| `session` | `string` | Session token for using the api, expires after 10min of not using the api |

### Mark card as done
#### Request

```http
  POST /api/cards/done
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `session`      | `string` | **Required**. Session token |
| `input`      | `string` | **Required**. Input for the guess |
| `id`      | `int32` | **Required**. Card Id |

#### Respone
| Code  | Meaning             |
|:------|:--------------------| 
| `200` | `Sucess`            |
| `204` | `No card left`      |
| `400` | `Fields empty`      |
| `401` | `No valid session ` |

| Parameter | Type     | Description                                         |
|:----------|:---------|:----------------------------------------------------|
| `time`    | `int64`  | Unix-timestamp when the card is prompted up  next.  |
| `correct` | `string` | Contains the correct answer. Is empty if incorrect. |
| `others`  | `string` | Other possible answers. Separated with `>>>`        |

### Request new card
#### Request

```http
  POST /api/cards/now
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `session`      | `string` | **Required**. Session token |
| `group`      | `string` | **Optional**. Session token |

#### Respone
| Code  | Meaning             |
|:------|:--------------------| 
| `200` | `Sucess`            |
| `204` | `No card left`      |
| `400` | `Bad request`       |
| `401` | `No valid session ` |

| Parameter  | Type     | Description                           |
|:-----------|:---------|:--------------------------------------|
| `id`       | `int32`  | Id of the card                        |
| `question` | `string` | Question of the card. It may be html. |

### Get card categories
#### Request

```http
  GET /api/cards/groups
```

| Parameter | Type | Description |
|:----------|:-----|:------------|
| `-`       | `-`  | -           |

#### Respone
| Code  | Meaning                 |
|:------|:------------------------| 
| `200` | `Sucess`                |
| `500` | `Internal server error` |

| Parameter | Type     | Description                                       |
|:----------|:---------|:--------------------------------------------------|
| `groups`  | `string` | List of the card categories. Separated with `>>>` |

### Get image
#### Request

```http
  GET /api/image/${token}
```

| Parameter | Type | Description |
|:----------|:-----|:------------|
| `-`       | `-`  | -           |

#### Respone
| Code  | Meaning                   |
|:------|:--------------------------| 
| `200` | `Sucess`                  |
| `400` | `Bad request`             |
| `404` | `The image was not found` |

| Parameter | Type | Description |
|:----------|:-----|:------------|
| `-`       | `-`  | -           |

## Related
See the frontend for the IndexCards app:
[IndexCards-Frontend](https://github.com/pianoman911/IndexCards-Frontend)