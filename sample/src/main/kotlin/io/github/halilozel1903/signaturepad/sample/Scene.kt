package io.github.halilozel1903.signaturepad.sample

/** Screenshot scenes, picked with the `scene` intent extra. */
enum class Scene(val key: String) {
    Signature("signature"),
    Sketch("sketch"),
    Export("export"),
    ;

    companion object {
        fun from(key: String?): Scene? = entries.firstOrNull { it.key == key }
    }
}
