package com.guitarvisualizer.config

import com.typesafe.config.{Config, ConfigFactory}

case class DatabaseConfig(
  url: String,
  username: String,
  password: String,
  maxPoolSize: Int = 10
)

case class ServerConfig(
  host: String = "0.0.0.0",
  port: Int = 8080
)

case class AppConfig(
  database: DatabaseConfig,
  server: ServerConfig
)

object AppConfig {
  def load(): AppConfig = {
    val config: Config = ConfigFactory.load()
    
    // Try to get database URL from environment first
    val databaseUrl = sys.env.getOrElse("DATABASE_URL", 
      config.getString("database.url"))
    
    // Parse DATABASE_URL if it's in the postgresql://user:password@host:port/dbname format
    val (dbUrl, dbUser, dbPassword) = parseDatabaseUrl(databaseUrl)
    
    AppConfig(
      database = DatabaseConfig(
        url = dbUrl,
        username = dbUser,
        password = dbPassword,
        maxPoolSize = config.getInt("database.maxPoolSize")
      ),
      server = ServerConfig(
        host = config.getString("server.host"),
        port = config.getInt("server.port")
      )
    )
  }
  
  private def parseDatabaseUrl(url: String): (String, String, String) = {
    if (url.startsWith("postgresql://")) {
      val pattern = """postgresql://([^:]+):([^@]+)@([^:]+):(\d+)/(.+)""".r
      url match {
        case pattern(user, password, host, port, dbname) =>
          (s"jdbc:postgresql://$host:$port/$dbname", user, password)
        case _ =>
          throw new IllegalArgumentException(s"Invalid DATABASE_URL format: $url")
      }
    } else {
      // Assume it's already a JDBC URL
      val user = sys.env.getOrElse("DB_USERNAME", "admin")
      val password = sys.env.getOrElse("DB_PASSWORD", "password")
      (url, user, password)
    }
  }
}
