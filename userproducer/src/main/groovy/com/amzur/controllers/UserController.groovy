package com.amzur.controllers

import com.amzur.producer.MessageProducer
import com.amzur.userentities.User
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
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
    @Client("http://localhost:8080") //URL of other microservice
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

    @ExecuteOn(TaskExecutors.IO)
    @Get
    @Status(HttpStatus.CREATED)
    def getAllUsers(){
        HttpResponse<List<User>> response=httpClient.toBlocking().exchange(HttpRequest.GET("/users-process"), Argument.listOf(User))
        //HttpRequest us used when the get url
        if(response.status==HttpStatus.OK && response.body){
            List<User> userList=response.body()
            return HttpResponse.ok(userList)
        }
        else{
            return HttpResponse.status(response.status).body("Failed to get users")
        }
    }

    @ExecuteOn(TaskExecutors.IO)
    @Put("/{id}")
    @Status(HttpStatus.CREATED)
    def updateUsersById(@PathVariable Long id, @Body User user){
        HttpResponse<User> response=httpClient.toBlocking().exchange(HttpRequest.PUT("/users-process/${id}",user),User)
        //Response type is HttpResponse and User Entity
        if(response.status==HttpStatus.OK && response.body){
            def updatedUser=response.body()
            if(messageProducer.sendMessage(updatedUser)){
                return HttpResponse.ok("Updated user successfully")
            }
            else{
                return HttpResponse.serverError("Unable to send upated user")
            }
        }
        else{
            return HttpResponse.status(response.status).body("Failed to Update User ")
        }
    }

    @ExecuteOn(TaskExecutors.IO)
    @Delete("/{id}")
    @Status(HttpStatus.CREATED)
    def deleteUserById(@PathVariable Long id){
        HttpResponse<?> response=httpClient.toBlocking().exchange(HttpRequest.DELETE("/users-process/${id}"))
        if(response.status()==HttpStatus.NO_CONTENT && response.body){
            if(messageProducer.sendMessage("Deleted User successfully")){
                return HttpResponse.ok("Deleted User")
            }
            else{
                return HttpResponse.serverError("Error Deleting User")
            }

        }
        else{
            return  HttpResponse.status(response.status()).body("Failed to Delete User in other microservice")
        }
    }
}
