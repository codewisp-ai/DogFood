package com.dogfood.voting.service;

import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.ArrayList;

@Service
public class BallotService {
    public List<UUID> getRandomizedBallot(UUID eventId, UUID voterId, List<UUID> submissionIds) {
        long seed = (voterId.toString() + eventId.toString()).hashCode();
        Random rnd = new Random(seed);
        List<UUID> result = new ArrayList<>(submissionIds);
        Collections.shuffle(result, rnd);
        return result;
    }
}
