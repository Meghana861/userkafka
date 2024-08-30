package com.amzur.controllers

import com.amzur.producer.MessageProducer
import com.amzur.userentities.User
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.http.HttpStatus
import jakarta.inject.Inject



@Controller("/users")
class UserController {
    @Inject
    MessageProducer messageProducer
    @Inject
    @Client("http://localhost:9090") //URL of other microservice
    HttpClient httpClient //used to make http requests

    @ExecuteOn(TaskExecutors.BLOCKING)
    @Post
    @Status(HttpStatus.CREATED)
    def createUser(@Body User user){
        try{
        HttpResponse<User> response = httpClient.toBlocking().exchange(HttpRequest.POST("/users-process",user),User)
            //user-request body being sent to the post
            //User-Type of response body that you expect to be received
        if(response.status==HttpStatus.CREATED&&response.body) {
            User savedUser = response.body()

            if (messageProducer.sendMessage(savedUser)) {
                return HttpResponse.ok("Sent user successfully")
            } else {
                return HttpResponse.serverError("Unable to send objects through kafka")
            }
        }
            else{
            return HttpResponse.status(response.status).body("Failed to process user request in the other microservice")
        }
        }
        catch (Exception e){
            return  HttpResponse.serverError("An error occurred: ${e.message}")
        }
    }
}
