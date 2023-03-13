package io.github.alessandrojean.tankobon.infrastructure.search

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.LowerCaseFilter
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.Tokenizer
import org.apache.lucene.analysis.cjk.CJKBigramFilter
import org.apache.lucene.analysis.cjk.CJKWidthFilter
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter
import org.apache.lucene.analysis.standard.StandardTokenizer

open class MultiLingualAnalyzer : Analyzer() {
  override fun createComponents(fieldName: String): TokenStreamComponents {
    val source: Tokenizer = StandardTokenizer()

    val filter: TokenStream = CJKWidthFilter(source)
      .let(::LowerCaseFilter)
      .let(::CJKBigramFilter)
      .let(::ASCIIFoldingFilter)

    return TokenStreamComponents(source, filter)
  }

  override fun normalize(fieldName: String?, `in`: TokenStream): TokenStream {
    return CJKWidthFilter(`in`)
      .let(::LowerCaseFilter)
      .let(::ASCIIFoldingFilter)
  }
}