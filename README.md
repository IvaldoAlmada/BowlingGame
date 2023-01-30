# BowlingGame

## Steps to run the application

- Generate the application jar running `sbt assembly`
- Start postgres database running `docker-compose up`
- import the `BowlingGame.postman_collection.json` file to your postman application.
- Run the main class `com.game.bowling.HttpServer`
## Available endpoints

- GET /api/game/{gameId}
- GET /api/game/{gameId}/score
- POST /api/game
```json5
{
    "name": "5e5a39bb-a497-4432-93e8-7322f16ac0b4",
    "complete": false
}
```
- PUT /api/game/{gameId}/roll
```json5
{
    "score": 10
}
```
- DELETE /api/game/{gameId}