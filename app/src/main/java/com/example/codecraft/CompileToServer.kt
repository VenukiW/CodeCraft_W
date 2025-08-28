package com.example.codecraft


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Socket

fun compileCodeToServer(
    code: String,
    onOutput: (String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val socket = Socket("10.0.2.2", 5000) // Desktop server port via ADB
            val writer = socket.getOutputStream().bufferedWriter()
            val reader = socket.getInputStream().bufferedReader()

            // Send code to the server
            writer.write(code)
            writer.flush()
            socket.shutdownOutput() // signal end of input

            // Read output/errors
            val output = reader.readText()

            // Return to UI
            withContext(Dispatchers.Main) {
                onOutput(output)
            }

            socket.close()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onOutput("Error connecting to compiler server:\n${e.message}")
            }
        }
    }
}
