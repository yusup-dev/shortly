package main

import (
	"os"
	"shortly-api-service/config"
	"shortly-api-service/internal/database"
	"shortly-api-service/internal/redis"
	"shortly-api-service/internal/utils"
)

func main() {

	utils.InitLogger()

	if err := config.Init(); err != nil {
		utils.Log.Error("Failed to load env", "error", err)
		os.Exit(1)
	}

	utils.Log.Info("Environment variables loaded successfully")

	if err := database.ConnectDB(); err != nil {
		utils.Log.Error("Failed to connect to database", "error", err)
		os.Exit(1)
	}

	if err := redis.ConnectRedis(); err != nil {
		utils.Log.Error("Failed to connect to Redis", "error", err)
		os.Exit(1)
	}

	defer redis.RedisClient.Close()
}
