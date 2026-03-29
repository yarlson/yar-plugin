package dev.yarlson.yar.references

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import dev.yarlson.yar.YarLanguage

class YarReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement().withLanguage(YarLanguage),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    val ref = YarReference.create(element) ?: return PsiReference.EMPTY_ARRAY
                    return arrayOf(ref)
                }
            }
        )
    }
}
