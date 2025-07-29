package com.guitarvisualizer.database

import com.guitarvisualizer.config.DatabaseConfig
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory

object DatabaseMigration {
  private val logger = LoggerFactory.getLogger(getClass)
  
  def migrate(config: DatabaseConfig): Unit = {
    logger.info("Starting database migration...")
    
    val flyway = Flyway.configure()
      .dataSource(config.url, config.username, config.password)
      .baselineOnMigrate(true)
      .locations("classpath:db/migration")
      .load()
    
    try {
      val result = flyway.migrate()
      logger.info(s"Database migration completed. Applied ${result.migrationsExecuted} migrations.")
    } catch {
      case ex: Exception =>
        logger.error("Database migration failed", ex)
        throw ex
    }
  }
}
