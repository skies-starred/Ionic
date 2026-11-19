package foo.starred.ionic.module.impl.general.fish.data

import foo.starred.ionic.api.location.island.impl.PresetSkyBlockIsland

enum class AutoFishSeaCreatureData(val island: PresetSkyBlockIsland?, val messages: Set<String>) {
    WATER(
        null,
        setOf(
            "The Water Hydra has come to test your strength.",
            "Is this even a fish? It's the Carrot King!",
            "A Squid appeared.",
            "You caught a Sea Walker.",
            "It looks like you've disrupted the Sea Witch's brewing session. Watch out, she's furious!",
            "You reeled in a Sea Archer.",
            "The Rider of the Deep has emerged.",
            "Huh? A Catfish!",
            "Gross! A Sea Leech!",
            "You've discovered a Guardian Defender of the sea.",
            "You have awoken the Deep Sea Protector, prepare for a battle!",
            "Your Chumcap Bucket trembles, it's an Agarimoo.",
        )
    ),
    WATER_HOTSPOTS(
        null,
        setOf(
            "You get an inkling that you've caught... an Inkling!",
            "A majestic creature rises from the water. It's a Manta Ray.",
            "Is it a frog? Is it a man? Well, yes, sorta, IT'S FROG MAN!!!!!!",
            "A Snapping Turtle is coming your way, and it's ANGRY!",
            "A garish set of tentacles arise. It's a Blue Ringed Octopus!",
            "The water bubbles and froths. A massive form emerges- you have disturbed the Wiki Tiki! You shall pay the price.",
        )
    ),
    FISHING_FESTIVAL(
        null,
        setOf(
            "Hide no longer, a Great White Shark has tracked your scent and thirsts for your blood!",
            "A tiny fin emerges from the water, you've caught a Nurse Shark.",
            "You spot a fin as blue as the water it came from, it's a Blue Shark.",
            "A striped beast bounds from the depths, the wild Tiger Shark!",
        )
    ),
    WINTER(
        PresetSkyBlockIsland.JERRYS_WORKSHOP,
        setOf(
            "What is this creature!?",
            "A Reindrake forms from the depths.",
            "You found a forgotten Nutcracker laying beneath the ice.",
            "Frozen Steve fell into the pond long ago, never to resurface...until now!",
            "It's a snowman! He looks harmless.",
            "The Grinch stole Jerry's Gifts...get them back!",
        )
    ),
    SPOOKY(
        null,
        setOf(
            "The spirit of a long lost Phantom Fisher has come to haunt you.",
            "This can't be! The manifestation of death himself!",
            "Phew! It's only a Scarecrow.",
            "You hear trotting from beneath the waves, you caught a Nightmare.",
            "It must be a full moon, a Werewolf appears.",
            "Watch out! It's Jumpin' Jack.",
        )
    ),
    CRIMSON_ISLE(
        PresetSkyBlockIsland.CRIMSON_ISLE,
        setOf(
            "Smells of burning. Must be a Fried Chicken.",
            "Trouble's brewing, it's a Fireproof Witch!",
            "From beneath the lava appears a Magma Slug.",
            "You hear a faint Moo from the lava... A Moogma appears.",
            "A small but fearsome Lava Leech emerges.",
            "You feel the heat radiating as a Pyroclastic Worm surfaces.",
            "A Lava Flame flies out from beneath the lava.",
            "A Fire Eel slithers out from the depths.",
            "Taurus and his steed emerge.",
            "You feel a burning sensation as you reel in a Volcanic Snail!",
            "A Magma Pillar rises from the lava.",
            "A Fiery Scuttler inconspicuously waddles up to you, friends in tow.",
            "You hear a massive rumble as Thunder emerges.",
            "You have angered a legendary creature... Lord Jawbus has arrived.",
            "WOAH! A Plhlegblast appeared.",
            "The sky darkens and the air thickens. The end times are upon us: Ragnarok is here."
        )
    ),
    OASIS(
        PresetSkyBlockIsland.THE_BARN,
        setOf(
            "An Oasis Rabbit appears from the water.",
            "An Oasis Sheep appears from the water.",
        )
    ),
    CRYSTAL_HOLLOWS(
        PresetSkyBlockIsland.CRYSTAL_HOLLOWS,
        setOf(
            "An Abyssal Miner breaks out of the water!",
            "A Water Worm surfaces!",
            "A Poisoned Water Worm surfaces!",
            "A Flaming Worm surfaces from the depths!",
            "A Lava Blaze has surfaced from the depths!",
            "A Lava Pigman arose from the depths!",
        )
    ),
    ABANDONED_QUARRY(
        PresetSkyBlockIsland.DWARVEN_MINES,
        setOf(
            "A leech of the mines surfaces... you've caught a Mithril Grubber.",
            "A leech of the mines surfaces... you've caught a Medium Mithril Grubber.",
            "A leech of the mines surfaces... you've caught a Large Mithril Grubber.",
            "A leech of the mines surfaces... you've caught a Bloated Mithril Grubber.",
        )
    ),
    BACKWATER_BAYOU(
        PresetSkyBlockIsland.BACKWATER_BAYOU,
        setOf(
            "The Trash Gobbler is hungry for you!",
            "A Dumpster Diver has emerged from the swamp!",
            "The desolate wail of a Banshee breaks the silence.",
            "A swampy mass of slime emerges, the Bayou Sludge!",
            "A long snout breaks the surface of the water. It's an Alligator!",
            "A massive Titanoboa surfaces. Its body stretches as far as the eye can see.",
        )
    ),
    MOONGLADE_MARSH(
        PresetSkyBlockIsland.GALATEA,
        setOf(
            "You've caused a disturbance in the loch. Could it be... Nessie?",
            "The Loch Emperor arises from the depths.",
            "A gang of Liltads!",
            "You've hooked an Ent, as ancient as the forest itself.",
            "Look! A Wetwing emerges!",
            "You caught a Stridersurfer.",
            "You've hooked a Bogged!",
        )
    ),
    LOTUS_ATOLL(
        PresetSkyBlockIsland.LOTUS_ATOLL,
        setOf(
            "An inquisitive Atoll Croaker takes the bait!",
            "A Lotus Guardian emerges, ready to protect the Atoll.",
            "A Drowned Captain takes hold of your bobber!",
            "What even is that?! A... gorF?",
            "A Puddle Jumper is preparing for liftoff—cast your rod into it and hold on tight!",
            "Bow down before the Frog Prince... or pay the hefty price!",
        )
    ),
    TORRHUS_CANYON(
        PresetSkyBlockIsland.TORRHUS_CANYON,
        setOf(
            "A Haggard stumbles to the shore, ready for a fight!",
            "A Brineling interrupts you with a stream of bubbles!",
            "A Sprawl emerges from the blue, and it's looking for you!",
            "The laughter of a Torrid echoes through the air.",
            "Something zips through the air - it's a Silkbreeze!",
            "A Giant Isopod was dredged up from the depths!",
        )
    );

    companion object {
        private val island = entries.filter { it.island != null }
        private val global = entries.filter { it.island == null }

        fun find(message: String): AutoFishSeaCreatureData? {
            return island.firstOrNull { it.island?.active == true && message in it.messages } ?: global.firstOrNull { message in it.messages }
        }
    }
}
