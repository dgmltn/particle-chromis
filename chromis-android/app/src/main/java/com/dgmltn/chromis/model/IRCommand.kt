package com.dgmltn.chromis.model

import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class IRCommand(
        @PrimaryKey open var id: Long = 0,
        open var name: String = "",
        @Index open var command: String = "",
        open var description: String = ""
) : RealmObject()
