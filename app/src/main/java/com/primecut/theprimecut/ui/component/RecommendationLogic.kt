package com.primecut.theprimecut.ui.component

/**
 * Generates tactical commands based on current nutritional state.
 */
fun getNextMove(
    proteinCurrent: Float, proteinGoal: Float,
    carbsCurrent: Float, carbsGoal: Float,
    fatsCurrent: Float, fatsGoal: Float
): List<String> {
    val commands = mutableListOf<String>()
    
    when {
        fatsCurrent > fatsGoal -> commands.add("CRITICAL: FAT LIMIT EXCEEDED. SWITCH TO LEAN WHITE FISH OR STEAMED GREENS.")
        proteinCurrent < proteinGoal -> commands.add("ACTION REQUIRED: PROTEIN DEFICIT DETECTED. DEPLOY EGG WHITES OR ISOLATE.")
        carbsCurrent < carbsGoal * 0.4f -> commands.add("CAUTION: GLYCOGEN RESERVES CRITICALLY LOW. INTEGRATE COMPLEX CARBS.")
    }
    
    return commands.take(2)
}
