# Opening Hours

## Requirements

- SBT 1.8.2
- JDK 19
  Or alternatively
- Docker (or Podman or something if that's how you roll)

## How to containerize again after making changes
You will need SBT installed.

```shell
make containerize
```

## How to run the server

```shell
sbt run
```

or alternatively if you do not have SBT installed there is a jar included in the build folder. Build a docker image with
this jar using the dockerfile. If you do have SBT installed you can also make any changes and containerize it again 
yourself. See the section below.

```shell
docker build -t wolt-backend-assignment .
```

and then create a container with the image.

```shell
docker run -p 8080:8080 wolt-backend-assignment
```

or simply

```shell
make run
```

## How to containerize again after making changes
You will need SBT installed.

```shell
make containerize
```

## How to run tests

`sbt test` or `make test`

```shell    
curl --location '0.0.0.0:8080/opening-hours' \
--header 'Content-Type: application/json' \
--data '{"monday":[],"tuesday":[{"type":"open","value":36000},{"type":"close","value":64800}],"wednesday":[],"thursday":[{"type":"open","value":37800},{"type":"close","value":64800}],"friday":[{"type":"open","value":36000}],"saturday":[{"type":"close","value":3600},{"type":"open","value":36000}],"sunday":[{"type":"close","value":3600},{"type":"open","value":43200},{"type":"close","value":75600}]}'
```

Should give back:

```
Monday: Closed
Tuesday: 10 AM - 6 PM
Wednesday: Closed
Thursday: 10:30 AM - 6 PM
Friday: 10 AM - 1 AM
Saturday: 10 AM - 1 AM
Sunday: 12 PM - 9 PM
```

## How the input is interpreted

- Missing weekdays are treated as parsing errors. The input is expected to include all the weekdays.
- Overlapping or unbalanced opening hours are also treated as parsing errors.
- If there are multiple opening hours in the same day they will be put in a comma separated list.
- If the closing time corresponding to the opening time occurs on a future day it will be interpreted like the following
    - if the closing day is in the immediate next day and the closing time is less than 24 hours after the opening time
      it
      will be interpreted as the closing time being on the same day.
    - Otherwise, the actual closing day will be used and for each day in between the opening hours will be interpreted
      as
      open 24 hours.

## A word about the input format

There is nothing inherently wrong with this format. As with all things in software, there are tradeoffs. If I could
choose the format in which I would receive the input I would probably go for something like the Google Places API:

```json
[
  {
    "close": {
      "day": 1,
      "time": "1700"
    },
    "open": {
      "day": 1,
      "time": "0900"
    }
  },
  {
    "close": {
      "day": 2,
      "time": "1700"
    },
    "open": {
      "day": 2,
      "time": "0900"
    }
  },
  {
    "close": {
      "day": 3,
      "time": "1700"
    },
    "open": {
      "day": 3,
      "time": "0900"
    }
  },
  {
    "close": {
      "day": 4,
      "time": "1700"
    },
    "open": {
      "day": 4,
      "time": "0900"
    }
  },
  {
    "close": {
      "day": 5,
      "time": "1700"
    },
    "open": {
      "day": 5,
      "time": "0900"
    }
  }
]
```
This format more closely represents the way I modelled the data in my application, so it would have been easier to
implement and therefore introduce fewer opportunities for bugs.
