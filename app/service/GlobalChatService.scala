package service

import actor_register.GlobalChatActorRegister
import akka.actor.ActorSystem
import helper.{WebSocketResponseBuilder, WebSocketResponseType}
import model.GlobalChatMessage
import play.api.mvc.{AnyContent, Request}
import serializer.ChatMessageJsonSerializer
import validation.chat_message.GlobalChatMessageValidator

class GlobalChatService {

  def handleGlobalChatMessageBroadcast(actorSystem: ActorSystem, request: Request[AnyContent]): Boolean = {
    val chatMessage = ChatMessageJsonSerializer.fromJson(request.body.asJson.get.toString())
    val isMessageValid = GlobalChatMessageValidator.validate(chatMessage)

    if(isMessageValid) {
      broadcastMessage(actorSystem, chatMessage)
    }

    isMessageValid
  }

  private def broadcastMessage(actorSystem: ActorSystem, chatMessage: GlobalChatMessage): Unit = {
    val globalChatActorRegister = new GlobalChatActorRegister(actorSystem)
    val chatMessageAsJson = buildResponseJson(ChatMessageJsonSerializer.toJson(chatMessage))

    globalChatActorRegister.broadcastMessage(chatMessageAsJson)
  }

  def buildResponseJson(globalChatMessageObjectAsJson: String): String = {
    val responseType = WebSocketResponseType.GLOBAL_CHAT

    WebSocketResponseBuilder.buildWebsocketResponse(responseType, globalChatMessageObjectAsJson)
  }
}
