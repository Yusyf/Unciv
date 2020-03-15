package com.unciv.app.desktop.textRpg

import com.unciv.app.desktop.*

object Locations{
    const val academy = "Academy (Cyoria)"
    const val alchemyLab = "Alchemical lab"
    const val library = "Library"
    const val home = "Home (Cirin)"
}

class PreparationState(val player: Player) : State() {
    var preparationTurns = 6
    var location = Locations.home

    fun getLocationChoices(){
        val choices = when (location) {
            Locations.academy -> academyChoices()
            Locations.alchemyLab -> alchemyLabChoices()
            Locations.library -> libraryChoices()
            else -> homeChoices()
        }
        chooseAndActivateAction(choices)
    }

    private fun alchemyLabChoices(): List<Action> {
        if (player.addEncounter(Encounter.alchemyLab)) {
            println("The academy has an alchemical workshop students could use for their own projects, but you have to bring your own ingredients.")
        }
        println("There are a couple of people here, but there's still space to work.")
        return listOf(Action("Leave") { location = Locations.academy; getLocationChoices() })
    }

    private fun libraryChoices(): List<Action> {
        if (player.addEncounter(Encounter.library)) {
            val texts = listOf(
                    "Although the academy loved saying they were an elite institution thanks to the excellent quality of its teaching staff,",
                    " the truth was that the main reason for their supremacy was their library.",
                    " Through contributions of its alumni, generous budget allocations by a number of former headmasters,",
                    " quirks of local criminal law, and sheer historical accident, the academy had built a library without equal.",
                    " You could find anything you wanted, regardless of whether the topic was magical or not",
                    " – there was a whole section reserved for steamy romance novels, for instance.",
                    " The library was so massive it had actually expanded into the tunnels beneath the city.",
                    " Many of the lower levels are only accessible to guild mages,",
                    " so it is only now you are allowed to browse their contents.")
            texts.forEach { println(it) }
        }

        return listOf(Action("Search for books about time travel") {
            listOf(
                    "To call it disappointing would be calling it mildly. ",
                    "For one thing, there are no books on time travel. ",
                    "The topic is not considered a serious field of study, what with it being impossible and all. ",
                    "What little is written about it is scattered across innumerable volumes, ",
                    "   hidden in unmarked sections and paragraphs of otherwise unrelated books. ",
                    "Piecing together these scattered mentions was an absolute chore, and not all that rewarding either – ",
                    "   none of it was useful in solving the mystery of the future memories."
            ).forEach { println(it) }
            getLocationChoices()
        }.takeIf { Encounter.reset in player.encounters && player.addEncounter(Encounter.searchTimeTravelBooks) },
                Action("Search around aimlessly"){
                    println("You wander the library for a bit, but you're not sure what you're looking for," +
                            " so it'll be hard for you to find anything useful.")},
                Action("Leave") { location = Locations.academy; getLocationChoices() })
                .filterNotNull()
    }

    override fun nextState(): State {
        getLocationChoices()

        println("   ------    ")

        if (preparationTurns > 0) {
            println("It's a new day - What will you do?")
            return this
        } else {
            return InvasionState(player)
        }
    }

    fun academyChoices(): List<Action> {
        if(Encounter.reset in player.encounters && player.addEncounter(Encounter.thinkAboutWarningOthers)) {
            listOf("The simplest idea would be to warn as many people as possible ",
                    "(thus ensuring that at least some of them take the warnings seriously) and do so face-to-face,",
                    " since written communications can be ignored in a way that is not really possible in personal interactions.",
                    " Unfortunately, that would almost certainly paint you as a madman until you're eventually vindicated by the actual assault.")
                    .forEach { println(it) }
        }

        return listOf(Action("Go to class"){
            classChoice()
            preparationTurns--
            },

            Action("Practice"){
                val practiceActions = player.abilities.map { Action("Practice "+it.name){
                    println("You practice ${it.name} for a day.")
                    it.experience += 10
                    preparationTurns--
                } }
                chooseAndActivateAction(practiceActions)
            }.takeIf { player.encounters.contains(Encounter.reset) },
            Action("Go to..."){
                val locations = listOf(Locations.alchemyLab, Locations.library)
                val locationActions = locations.map {
                    Action("Go to $it") {
                        location = it
                        getLocationChoices()
                    }
                }
                chooseAndActivateAction(locationActions)
            }
        ).filterNotNull()
    }

    fun classChoice(){

        if(Encounter.reset in player.encounters) println("It's exactly like it was the previous time.")
        when(preparationTurns) {
            5 -> {
                println("Essential Invocations. We learned the difference between structured and unstructured magic," +
                        " and learned the 'torch' spell and how it differs from the light-emitting shaping exercise.")
                if (player.abilities.none { it.name == "Mana shaping exercise" })
                    player.abilities += Ability("Torch", arrayListOf("Light emitting"), listOf())
            }
            4 -> println("Basic Runes. This year, we're starting to learn about imbuing runes, and the effects of such.")
            3 -> {
                println("Warding. The teacher's textbook is more like a manual for a professional warder than a student's textbook,")
                println(" and it's clear that although she knows the subject material, she doesn't know how to teach. At all.")
            }
            2 -> {
                println("Advanced mana control. We learnt a new mana shaping exercise - levitating, and *spinning*, a pencil.")
                println("It's much more difficult than I expected.")
                if (player.abilities.none { it.name == "Mana shaping exercise" })
                    player.abilities += Ability("Mana shaping exercise", arrayListOf("Mana Shaping"), listOf())
            }
            1 -> {
                println("Alchemy. We made a type of glue out of ground Borer husks.")
            }
        }
        if(Encounter.reset in player.encounters) println("It's exactly like it was the previous time.")
    }

    fun homeChoices(): List<Action> {
        return listOf(Action("Head to Cyoria"){
            location = Locations.academy
            if(!player.encounters.contains(Encounter.reset)) {
                println("You take the train from Cirin to Cyoria, and check in to your new academy housing -")
                println("  since the Academy is now authorized to teach you first-circle spells, security is tighter.")
            }
            else {
                println("You take the train to Cyoria again.")
                if(Encounter.tookTrainBack in player.encounters && player.addEncounter(Encounter.musings)) {
                    listOf("You stare at the endless fields blurring past you, the silence of the otherwise empty compartment ",
                            "only broken by the rhythmic thumping of the train's machinery.",
                            "You look calm and relaxed, but it's only a practiced façade and nothing more.",
                            "Why was this happening again? The first time it had happened, you were dead sure the mage was responsible.",
                            "The spell had hit you, and then you woke up in the past. Cause and effect. ",
                            "He hadn't been hit by some mysterious spell this time, though – ",
                            "not unless someone had snuck into the train compartment while he was sleeping, which he found very unlikely.",
                            " No, he had just dozed off and woke up in the past again, as if it was the most normal thing in the world.",
                            "Then again, it did highlight some things that had been bothering him until now. ",
                            "After all, why had the mage cast a time travel spell on him?",
                            " It seemed rather counterproductive to the whole 'secret invasion' plot.",
                            " Time travel seemed too purposeful and complex to be an accidental side effect,",
                            " and he seriously doubted the mage had used a spell whose effects he did not understand.",
                            " Even a neophyte like him knew what a horrible idea it was to use a spell you don't understand in an uncontrolled environment.",
                            " No, there was a simpler explanation: the mage wasn't responsible for his time traveling problems.",
                            "He really had been trying to kill him. ",
                            "He was half-tempted to get off the train and spend the entire month fooling",
                            " around and trying to forget this whole time travel business, but quickly dismissed it.",
                            " Blowing off the beginning of the school year like that would be really irresponsible and self-destructive,",
                            " even if going through another identical month of classes was anything but appealing.",
                            " There was the possibility that he would be flung into the past a third time time, of course,",
                            " but that wasn't something he should be relying on. ",
                            "There was no way the spell could keep sending him back indefinitely, after all – ",
                            "it was bound to run out of mana sooner or later.",
                            " Probably sooner, since time travel must be pretty high level."
                    ).forEach { println(it) }
                }
            }
            preparationTurns--
        })
    }
}

class NewGameState():State(){
    override fun nextState(): State {
        println("The summer break is finally over, and today you start your third year at Cyoria's Royal Academy of Magical Arts.")
        println("After passing the examination last year, you are now considered a proper first-circle mage.")
        val player = Player()
        return PreparationState(player)
    }
}