# Searching

Tankobon offers a search palette that is able to search through some
entities in the current selected library. You can open it by the
**Search** button in the navigation bar or by the [[Ctrl]] + [[K]]
shortcut in Windows/Linux or by the [[⌘]] + [[K]] shortcut in macOS.

![Search palette screenshot](/images/search-palette.png "Search palette with a sample search.")

## Keyboard navigation

Within the search palette, you can also navigate by the keyboard.

| Key             | Action                                    |
| --------------- | ----------------------------------------- |
| [[Enter ↵]]     | Select a result and navigate to its page. |
| [[↑]] and [[↓]] | Navigate through the results list.        |
| [[esc]]         | Close the search palette.                 |

## Full Text Search

Tankobon provides a [Full Text Search] (FTS hereafter) to return
relevant results from your libraries. The search is powered by
[Apache Lucene] and supports any features of Lucene [query syntax].

- FTS will order results by relevance.
- FTS is case-insensitive.
- When searching with CJK characters (Japanese, Chinese or Korean),
  a minimum of two characters is required.
- The order of words is not important: `jerry tom` will batch `Tom & Jerry`.
- To search by words in order, enclose your search in double quotes:
  `"white knight"` will not match `knight white` nor `white and knight`.
- By default, the search will match the item title. For books, the
  code will also be matched by default.
- You can use the `AND`, `OR`, and `NOT` logic operators (UPPERCASE)
  to build complex queries:

  - `contributor:"junji ito" NOT publisher:VIZ` will match all `Junji Ito`
    books not published by `VIZ`.
  - `tom OR jerry` will match `Tom` or `Jerry`.
  - `batman AND (robin OR superman)` will match `Superman & Batman` and
    `Batman & Robin`.
  - `writer:"ed brubaker" artist:"sean phillips"` will match any books
    where the script is done by `Ed Brubaker` and the art by `Sean Phillips`.
- You can perform range queries using the `[a TO b]` syntax. Example:
  `bought-at:[2020 TO 2023]`. Not that the matching is lexicographic, not
  numerical. You can also use wildcards: `bought-at:[2020 TO *]`.

  - Square brackets represents non inclusive range;
  - Curly brackets represents inclusive range.

Some extra fields are available for search using the `field:search` syntax,
see the next sessions for that.

[Full Text Search]: https://en.wikipedia.org/wiki/Full-text_search
[Apache Lucene]: https://lucene.apache.org
[query syntax]: https://lucene.apache.org/core/5_5_5/queryparser/org/apache/lucene/queryparser/classic/package-summary.html

## Entities additional fields

Some entities provides custom additional fields that can be used in the search.

### Series

`language`
: The original language of the series.

  Example: `language:ja-JP`.

`alternative-name`
: One of the alternative names of the series.

  Example: `alternative-name:ナウシカ`.

`series-type`
: Limit the results to only this series' type.

  Possible values: `unknown`, `manga`, `manhwa`, `manhua`, `comic`, `book`,
  `novel`, `databook`, `artbook` or `light-novel`.

  Example: `series-type:manga`.

### Book

`subtitle`
: The book subtitle.

  Example: `subtitle:"life story"`.

`code`
: The book code. Can be its ISBN, EAN-13 or ISSN.

  Example: `code:9781941220979`.

`tag`
: One of the book tags must match the search.

  Example: `tag:vertigo`.

`publisher`
: One of the book publishers must match the search.

  Example: `publisher:VIZ`.

`library`
: The library name the book belongs must match be the search.

  Example: `library:"my library"`.

`contributor`
: One of the book contributors must match the search.

  Example: `contributor:"junji ito"`.

  You can also specify the role the contributor has.

  Example: `Artist:"junji ito"`.

### Publisher

`legal-name`
: The legal name of the publisher.

  Example: `legal-name:"VIZ Media"`.

### Store

`legal-name`
: The legal name of the store.

  Example: `legal-name:"Amazon.com, Inc"`.

### Person

`native-name`
: The native name of the person.

  Example: `native-name:伊藤潤二`.

