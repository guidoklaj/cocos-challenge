package com.cocos.challenge.application.exception

class UserNotFoundException(userId: Int) :
    NotFoundException("User $userId not found")
