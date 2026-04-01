package dev.yarlson.yar.external

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test
import java.nio.file.Files

class YarExternalAnnotatorTest {

    @Test
    fun `prefers env and path before hardcoded fallbacks`() {
        val dir = Files.createTempDirectory("yar-external-annotator-test")
        try {
            val envYar = dir.resolve("env-yar").toFile().apply {
                writeText("#!/bin/sh\n")
                setExecutable(true)
            }
            val pathYar = dir.resolve("path-yar").toFile().apply {
                writeText("#!/bin/sh\n")
                setExecutable(true)
            }
            val fallbackYar = dir.resolve("fallback-yar").toFile().apply {
                writeText("#!/bin/sh\n")
                setExecutable(true)
            }

            assertEquals(
                envYar.absolutePath,
                YarExternalAnnotator.selectExecutable(
                    listOf(envYar.absolutePath, pathYar.absolutePath, fallbackYar.absolutePath)
                )
            )

            assertEquals(
                pathYar.absolutePath,
                YarExternalAnnotator.selectExecutable(
                    listOf(null, pathYar.absolutePath, fallbackYar.absolutePath)
                )
            )
        } finally {
            dir.toFile().deleteRecursively()
        }
    }

    @Test
    fun `skips missing or non executable candidates`() {
        val dir = Files.createTempDirectory("yar-external-annotator-test")
        try {
            val notExecutable = dir.resolve("not-executable").toFile().apply {
                writeText("plain text")
                setExecutable(false)
            }
            val executable = dir.resolve("yar").toFile().apply {
                writeText("#!/bin/sh\n")
                setExecutable(true)
            }

            assertEquals(
                executable.absolutePath,
                YarExternalAnnotator.selectExecutable(
                    listOf(notExecutable.absolutePath, executable.absolutePath)
                )
            )

            assertNull(
                YarExternalAnnotator.selectExecutable(
                    listOf(notExecutable.absolutePath, dir.resolve("missing-yar").toString())
                )
            )
        } finally {
            dir.toFile().deleteRecursively()
        }
    }
}
