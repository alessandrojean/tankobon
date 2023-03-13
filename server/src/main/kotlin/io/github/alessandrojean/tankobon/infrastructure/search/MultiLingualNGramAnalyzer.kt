package io.github.alessandrojean.tankobon.infrastructure.search

import org.apache.lucene.analysis.LowerCaseFilter
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.Tokenizer
import org.apache.lucene.analysis.cjk.CJKBigramFilter
import org.apache.lucene.analysis.cjk.CJKWidthFilter
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter
import org.apache.lucene.analysis.ngram.NGramTokenFilter
import org.apache.lucene.analysis.standard.StandardTokenizer

class MultiLingualNGramAnalyzer(
  private val minGram: Int,
  private val maxGram: Int,
  private val preserveOriginal: Boolean
) : MultiLingualAnalyzer() {

  override fun createComponents(fieldName: String): TokenStreamComponents {
    val source: Tokenizer = StandardTokenizer()

    val filter: TokenStream = CJKWidthFilter(source)
      .let(::LowerCaseFilter)
      .let(::CJKBigramFilter)
      .let { NGramTokenFilter(it, minGram, maxGram, preserveOriginal) }
      .let(::ASCIIFoldingFilter)

    return TokenStreamComponents(source, filter)
  }
}