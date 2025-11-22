package org.jbake.util

import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.Property
import java.io.PrintStream

class ConfigurationPrinter(private val configuration: JBakeConfiguration, private val out: PrintStream) {
    fun print() {
        val properties: MutableList<Property> = configuration.jbakeProperties
        var lastGroup: Property.Group? = null

        for (property in properties) {
            if (lastGroup != property.group) {
                printGroup(property)
                this.printHeader()
            }
            if (!property.description!!.isEmpty()) {
                printDescription(property)
            }
            printKeyAndValue(property)

            lastGroup = property.group
        }
    }

    private fun printHeader() {
        out.printf("%1$-40s %2$-40s%n", "Key", "Value")
        out.println(this.horizontalLine)
    }

    private val horizontalLine: String
        get() = String.format("%080d%n", 0).replace("0", "-")

    private fun printGroup(property: Property) {
        out.printf("%n%s - Settings%n%n", property.group)
    }

    private fun printDescription(property: Property) {
        out.printf("# %s%n", property.description)
    }

    private fun printKeyAndValue(property: Property) {
        val key = leftFillWithDots(property.key)
        val value = configuration.get(property.key)
        out.printf($$"%1$s: %2$-40s%n%n", key, value)
    }

    private fun leftFillWithDots(value: String): String {
        return String.format("%1$-40s", value).replace(' ', '.')
    }
}
