package pl.inventory.system

import pl.inventory.system.model.Item
import pl.inventory.system.model.Room
import pl.inventory.system.model.User

import java.time.LocalDate

class ObjectsProvider {
    User[] user = [
            User.builder()
                    .id(1)
                    .name("Default")
                    .surname("User")
                    .isInventoryUser(false)
                    .build(),
            User.builder()
                    .id(2)
                    .name("Inventory")
                    .surname("User 1")
                    .isInventoryUser(true)
                    .build(),
            User.builder()
                    .id(3)
                    .name("Inventory")
                    .surname("User 2")
                    .isInventoryUser(true)
                    .build()
    ]

    Item[] table = [
            Item.builder()
                    .inventoryNumber("PŚT-11/111")
                    .description("Computer table 1")
                    .incomingDate(LocalDate.of(2022, 05, 14))
                    .itemQuantity(1)
                    .itemPrice(BigDecimal.valueOf(482.19))
                    .documentNumber("BKM/2022/05")
                    .user(user[0])
                    .build(),
            Item.builder()
                    .inventoryNumber("PŚT-11/222")
                    .description("Computer table 2")
                    .incomingDate(LocalDate.of(2024, 3, 8))
                    .itemQuantity(1)
                    .itemPrice(BigDecimal.valueOf(316.22))
                    .documentNumber("-")
                    .user(user[1])
                    .build(),
            Item.builder()
                    .inventoryNumber("PŚT-11/333")
                    .description("Table")
                    .incomingDate(LocalDate.of(2020, 3, 8))
                    .outgoingDate(LocalDate.of(2024, 8, 28))
                    .itemQuantity(1)
                    .itemPrice(BigDecimal.valueOf(570.29))
                    .documentNumber("LRG/2024/08")
                    .user(user[1])
                    .build(),
            Item.builder()
                    .inventoryNumber("PŚT-11/444")
                    .description("Coffee table")
                    .incomingDate(LocalDate.of(2023, 4, 23))
                    .itemQuantity(1)
                    .itemPrice(BigDecimal.valueOf(224.09))
                    .documentNumber("STK/2023/04")
                    .user(user[2])
                    .build(),
    ]

    Item[] chair = [
            Item.builder()
                    .inventoryNumber("PŚT-22/111")
                    .description("Computer chair")
                    .incomingDate(LocalDate.of(2021, 2, 19))
                    .modificationDate(LocalDate.of(2021, 6, 9))
                    .itemQuantity(1)
                    .itemPrice(BigDecimal.valueOf(264.07))
                    .documentNumber("-")
                    .user(user[1])
                    .build(),
            Item.builder()
                    .inventoryNumber("PŚT-22/222")
                    .description("Chair")
                    .incomingDate(LocalDate.of(2021, 2, 19))
                    .itemQuantity(1)
                    .itemPrice(BigDecimal.valueOf(184.77))
                    .documentNumber("-")
                    .build(),

            Item.builder()
                    .inventoryNumber("PŚT-22/333")
                    .description("Armchair")
                    .incomingDate(LocalDate.of(2024, 6, 28))
                    .itemQuantity(1)
                    .itemPrice(BigDecimal.valueOf(889.99))
                    .documentNumber("FTS/2024/06")
                    .user(user[2])
                    .build(),
    ]

    Item[] wardrobe = [
            Item.builder()
                    .inventoryNumber("PŚT-33/111")
                    .description("Big document cupboard")
                    .incomingDate(LocalDate.of(2021, 7, 18))
                    .modificationDate(LocalDate.of(2021, 6, 9))
                    .itemQuantity(1)
                    .itemPrice(BigDecimal.valueOf(671.49))
                    .documentNumber("-")
                    .build(),
            Item.builder()
                    .inventoryNumber("PŚT-33/222")
                    .description("Small document cupboard")
                    .incomingDate(LocalDate.of(2023, 9, 28))
                    .itemQuantity(1)
                    .itemPrice(BigDecimal.valueOf(302.22))
                    .documentNumber("-")
                    .user(user[2])
                    .build(),
    ]

    Item[] computer = [
            Item.builder()
                    .inventoryNumber("PŚT-44/111")
                    .description("Computer Hewlet-Packard")
                    .incomingDate(LocalDate.of(2021, 7, 18))
                    .modificationDate(LocalDate.of(2021, 6, 9))
                    .itemQuantity(1)
                    .itemPrice(BigDecimal.valueOf(2890.29))
                    .documentNumber("-")
                    .user(user[0])
                    .build(),
            Item.builder()
                    .inventoryNumber("PŚT-44/222")
                    .description("Computer Acer")
                    .incomingDate(LocalDate.of(2023, 9, 28))
                    .modificationDate(LocalDate.of(2024, 3, 12))
                    .itemQuantity(1)
                    .itemPrice(BigDecimal.valueOf(3292.09))
                    .documentNumber("-")
                    .user(user[1])
                    .build(),
    ]
    Item[] printer = [
            Item.builder()
                    .inventoryNumber("PŚT-55/111")
                    .description("Printer Hewlet-Packard")
                    .incomingDate(LocalDate.of(2022, 7, 18))
                    .itemQuantity(1)
                    .itemPrice(BigDecimal.valueOf(490.89))
                    .documentNumber("-")
                    .build(),
            Item.builder()
                    .inventoryNumber("PŚT-55/222")
                    .description("Printer Brother")
                    .incomingDate(LocalDate.of(2023, 11, 20))
                    .modificationDate(LocalDate.of(2024, 3, 12))
                    .itemQuantity(1)
                    .itemPrice(BigDecimal.valueOf(670.11))
                    .documentNumber("-")
                    .user(user[2])
                    .build(),
    ]

    Room room1 = Room.builder()
            .roomNumber("101")
            .itemsList(Arrays.asList(
                    table[0],
                    chair[0],
                    chair[1],
                    wardrobe[0],
                    computer[0],
                    printer[0]
            ))
            .build()

    Room room2 = Room.builder()
            .roomNumber("102")
            .itemsList(Arrays.asList(
                    table[1],
                    table[3],
                    chair[0],
                    chair[1],
                    chair[2],
                    wardrobe[1],
                    computer[1],
                    printer[0]
            ))
            .build()

    Room room3 = Room.builder()
            .roomNumber("201")
            .itemsList(Arrays.asList(
                    table[0],
                    table[1],
                    table[2],
                    chair[0],
                    chair[1],
                    wardrobe[0],
                    wardrobe[1],
                    computer[0],
                    computer[1],
                    printer[1]
            ))
            .build()

    Room room4 = Room.builder()
            .roomNumber("208")
            .itemsList(Arrays.asList(
                    table[2],
                    table[3],
                    chair[1],
                    chair[2],
                    wardrobe[0],
                    wardrobe[1],
            ))
            .build()
}
