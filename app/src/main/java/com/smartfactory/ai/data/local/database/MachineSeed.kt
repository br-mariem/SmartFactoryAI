package com.smartfactory.ai.data.local.database

import com.smartfactory.ai.data.local.database.entity.MachineEntity

object MachineSeed {
    val machines = listOf(
        MachineEntity("PUMP_A1", "Pompe Hydraulique Principale", "Bâtiment A - Secteur 1", "POMPE", 85.0, 4.5),
        MachineEntity("PUMP_A2", "Pompe de Refroidissement", "Bâtiment A - Secteur 2", "POMPE", 85.0, 4.5),
        MachineEntity("COMP_B1", "Compresseur d'Air 1", "Bâtiment B - Secteur 1", "COMPRESSEUR", 90.0, 5.0),
        MachineEntity("COMP_B2", "Compresseur d'Air 2", "Bâtiment B - Secteur 2", "COMPRESSEUR", 90.0, 5.0),
        MachineEntity("MOT_C1", "Moteur Convoyeur Principal", "Bâtiment C - Secteur 1", "MOTEUR_ASYNCHRONE", 80.0, 4.0),
        MachineEntity("MOT_C2", "Moteur Convoyeur Secondaire", "Bâtiment C - Secteur 2", "MOTEUR_ASYNCHRONE", 80.0, 4.0),
        MachineEntity("MOT_C3", "Moteur Mélangeur", "Bâtiment C - Secteur 3", "MOTEUR_ASYNCHRONE", 80.0, 4.0),
        MachineEntity("CNC_D1", "Centre d'Usinage CNC 1", "Bâtiment D - Secteur 1", "MACHINE_OUTIL", 75.0, 3.5),
        MachineEntity("CNC_D2", "Centre d'Usinage CNC 2", "Bâtiment D - Secteur 2", "MACHINE_OUTIL", 75.0, 3.5),
        MachineEntity("PRESS_E1", "Presse Hydraulique", "Bâtiment E - Secteur 1", "PRESSE", 85.0, 6.0)
    )
}