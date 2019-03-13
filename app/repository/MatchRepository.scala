package repository


import java.sql.Timestamp

import javax.inject.{Inject, Singleton}
import model.{Match, Player}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MatchRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val playersRepository = new PlayerRepository(dbConfigProvider)

  import dbConfig._
  import profile.api._

  private class MatchesTable(tag: Tag) extends Table[Match](tag, "matches") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def startDate = column[Timestamp]("start_date")
    def endDate = column[Timestamp]("end_date")
    def playerId = column[Long]("player_id")
    def * = (id, startDate, endDate, playerId) <> ((Match.apply _).tupled, Match.unapply)
    def player = foreignKey("player", playerId, playersRepository.players)(_.id)
  }

  private val matches = TableQuery[MatchesTable]
  private val players = playersRepository.players

  def getAllMatches: Future[Seq[Match]] = db.run {
    matches.result
  }

  def getAllMatchesWithPlayers: Future[Seq[(Match, Player)]] = {
    val getMatchTableWithPlayerTableQuery =
      matches
        .join(players)
        .on(_.id === _.id)

    db.run {
      getMatchTableWithPlayerTableQuery.result
    }
  }

  def getMatchById(matchId: Long): Future[Option[Match]] = db.run {
    matches.filter(_.id === matchId).result.headOption
  }

  def getMatchByIdWithPlayer(matchId: Long): Future[Option[(Match, Player)]] = {
    val getMatchTableWithPlayerTableQuery =
      matches
        .join(players)
        .on(_.id === _.id)
        .filter(game => game._1.id === matchId)

    db.run {
      getMatchTableWithPlayerTableQuery.result.headOption
    }
  }

  def saveMatch(matchToSave: Match, playerId: Long): Future[Match] = {
    db.run {
      matches
        .filter(game =>
          matchToSave.startDate.after(Timestamp.valueOf(game.endDate.toString()))
          || matchToSave.endDate.after(Timestamp.valueOf(game.endDate.toString()))
        )
        .filter(game =>
          matchToSave.startDate.before(Timestamp.valueOf(game.startDate.toString()))
            || matchToSave.endDate.before(Timestamp.valueOf(game.startDate.toString()))
        )
        .filter(game => game.playerId === playerId)
        .result
    }.flatMap(games => {
      db.run {
        (
          matches.map(game =>
            (game.startDate, game.endDate, game.playerId)
          )

            returning matches.map(_.id)
            into ((data, id) => Match(id, data._1, data._2, data._3))

          ) += (matchToSave.startDate, matchToSave.endDate, playerId)
      }
    })


  }

  def deletePlayerMatchById(matchId: Long, playerId: Long): Future[Int] = db.run {
    matches
      .filter(_.id === matchId)
      .filter(_.playerId === playerId)
      .delete
  }
}

