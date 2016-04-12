package com.acuman;

import com.mongodb.MongoClient;
import org.springframework.stereotype.Service;

@Service
public class AcuService {

    MongoClient mongoClient = new MongoClient();

    public String findPatients() {
        return null;
    }
}
