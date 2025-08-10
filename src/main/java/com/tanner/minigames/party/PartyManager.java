package com.tanner.minigames.party;

import com.tanner.minigames.Minigames;
import org.bukkit.entity.Player;

import java.util.*;

public class PartyManager {

    private Minigames minigames;

    private List<Party> parties = new ArrayList<>();

    public PartyManager(Minigames minigames) {
        this.minigames = minigames;
    }

    public Party createParty(Player leader) {
        Party party = new Party(minigames, leader);
        parties.add(party);
        return party;
    }

    public Party getParty(Player player) {
        for (Party party : parties) {
            if (party.isInParty(player)) {
                return party;
            }
        }
        return null;
    }
}
