## v1.0.0 (2026-03-30)

### Features

-  Added a new interaction that is responsible for modifying statistics
-  Added a new component that tracks statistics like flask consumption
-  Added alphabetic sorting to the effect selection screen for easier navigation
-  Added scrolls that upgrade the maximum capacity of the flask
-  Added a new interaction that lets you modify the maximum capacity
-  Added recipe learning and "KnowledgeRequired" property to the scrolls and thus requiring the previous scroll to see the next ones
-  Added condition to check if player has 'MorningWakeUp' after sleeping
-  Added a new flask effect that lets given ore glow and sparkle for easier mining
-  Added the research table where players can craft new effects
-  Added the effects table that opens the effect selection screen when used
-  Added "Get" command for the capacity that displays the current and max capacity value of the target player
-  Added functionality to flask item.
-  Added interaction to modify uses for players flask at consumption
-  Added interaction to check if enough uses are available for players flask
-  Added command to reset max uses to players flask
-  Added command to set max uses to players flask
-  Added command to add max uses to players flask
-  Added the "RequireEffect" Interaction that acts as a condition to check if a specific effect is already known
-  Added the "ForgetEffect" Interaction that allows a player to forget the defined effect.
-  Added the "LearnEffect" Interaction that allows a player to learn the defined effect.
-  Added a resetMax capacity command that will remove the modifications done by commands
-  Added a setMax capacity command that lets you modify the maximum capacity to a specific value
-  Added an addMax capacity command, that lets you modify the maximum capacity with a command
-  Added speed effect that increases your movement speed for a duration
-  Added jump height effect, that increases your jump height for a duration
-  Added health improvement effect, that increases the maximum health
-  Added projectile resistance effect, that will improve the resistance for given amount of time.
-  Added ice resistance effect, that will improve the resistance for given amount of time.
-  Added physical resistance effect, that will improve the resistance for given amount of time.
-  Added poison resistance effect, that will improve the resistance for given amount of time.
-  Forget-Command now accepts `ALL` as an asset, which will forget all learned effects
-  Learn-Command now accepts `ALL` as an asset, which will learn all available effects
-  Added fire resistance effect, that will improve the resistance for given amount of time.
-  Added water breathing effect, that increases the oxygen or even allows for underwater breathing.
-  Added a recall effect. When you activate it and stand still for 5 seconds, you will be back at your spawn point.
-  Added the Stamina effect that increases your maximum stamina for a duration. It has level 1-3.
-  Added the HealthRegen effect in the level 1-4
-  FlaskEffect has the new field GroupDetails, where the FlaskEffectGroup and level can be defined
-  Added an asset field type for the FlaskEffectGroupDetails, which contains the ID of the FlaskEffectGroup and the level of the effect inside of that group
-  Added a new asset type FlaskEffectGroup, which represents a group of effects, so they can have multiple levels.
-  Added a command that opens the effects selection UI
-  Added a new entity stat "Capacity", that is a currency for equipping effects. You can equip multiple effects as long as you can pay their cost.
-  Added a command that displays all learned and active effects
-  Added a command that allows deactivating learned flask effects
-  Added a command that allows activating learned flask effects
-  Added a command that allows forgetting flask effects
-  Added a command that allows learning flask effects
-  Added a collection for the effects commands
-  Added the interaction `FlaskEffectApplyInteraction`
-  Added a player component that holds information about the learned and active effects
-  Added a configuration file for the HyFlask plugin
-  Added asset type for a `FlaskEffect`
-  Added command to set uses of players flask to specific amount
-  Added command to set uses of players flask to maximum
-  Added command to add uses to players flask

### Hytale Dependency Changes

-  Added Hytalor 2.2 as a required dependency

