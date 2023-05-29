import { SyntaxError, parse } from 'liqe'
import type { LiqeQuery } from 'liqe'

function highlightTag(ast: LiqeQuery, query: string) {
  if (ast.type !== 'Tag') {
    throw new Error('Expected a tag expression.')
  }

  const { expression, field, operator, location } = ast

  if (field.type === 'ImplicitField') {
    return query.slice(location.start, location.end)
  }

  const fieldValue = query.slice(field.location.start, field.location.end)
  const padBeforeOperator = '&nbsp;'.repeat(operator.location.start - field.location.end)
  const operatorValue = query.slice(operator.location.start, operator.location.end)
  const padBeforeExpression = '&nbsp;'.repeat(expression.location.start - operator.location.end)
  const expressionValue = query.slice(expression.location.start, expression.location.end)

  return `${fieldValue + padBeforeOperator + operatorValue + padBeforeExpression}<span class="tag-expression">${expressionValue}</span>`
}

function highlight(ast: LiqeQuery, query: string, root: LiqeQuery): string {
  const padStart = (ast === root ? '&nbsp;'.repeat(ast.location.start) : '')
  const padEnd = (ast === root ? '&nbsp;'.repeat(query.length - ast.location.end) : '')

  if (ast.type === 'ParenthesizedExpression') {
    if (!('location' in ast.expression)) {
      throw new Error('Expected location in expression.')
    }

    if (!ast.location.end) {
      throw new Error('Expected location end.')
    }

    /* eslint-disable prefer-template */
    return padStart + '('
      + '&nbsp;'.repeat(ast.expression.location.start - (ast.location.start + 1))
      + highlight(ast.expression, query, ast)
      + '&nbsp;'.repeat(ast.location.end - ast.expression.location.end - 1)
      + ')' + padEnd
  }

  if (ast.type === 'Tag') {
    const start = (ast.expression.type === 'RangeExpression' && ast.field.type === 'Field')
      ? ast.field.location.start
      : ast.location.start

    const padBefore = (ast === root ? '&nbsp;'.repeat(start) : '')

    return padBefore
      + highlightTag(ast, query)
      + padEnd
  }

  if (ast.type === 'LogicalExpression') {
    if (ast.operator.type === 'ImplicitBooleanOperator') {
      return padStart
        + highlight(ast.left, query, ast)
        + '&nbsp;'.repeat(ast.right.location.start - ast.left.location.end)
        + highlight(ast.right, query, ast)
        + padEnd
    }

    const operatorValue = `<span class="logic-operator">${query.slice(ast.operator.location.start, ast.operator.location.end)}</span>`

    return padStart
      + highlight(ast.left, query, ast)
      + '&nbsp;'.repeat(ast.operator.location.start - ast.left.location.end)
      + operatorValue
      + '&nbsp;'.repeat(ast.right.location.start - ast.operator.location.end)
      + highlight(ast.right, query, ast)
      + padEnd
  }

  if (ast.type === 'UnaryOperator') {
    const operatorValue = ast.operator === 'NOT'
      ? `<span class="logic-operator">${ast.operator}</span>`
      : ast.operator

    return padStart
      + operatorValue
      + '&nbsp;'.repeat(ast.operand.location.start - ast.location.start - ast.operator.length)
      + highlight(ast.operand, query, ast)
      + padEnd
  }

  return query
}

function highlightError(error: SyntaxError, query: string): string {
  console.error(error)
  return query.slice(0, error.offset)
    + `<span class="syntax-error">${query.slice(error.offset)}</span>`
}

export default function useLuceneQuery(query: Ref<string>) {
  const ast = computed(() => {
    try {
      return parse(query.value)
    } catch (error) {
      return (error instanceof SyntaxError) ? error : null
    }
  })

  const highlighted = computed(() => {
    if (!ast.value) {
      return query.value
    }

    try {
      return (ast.value instanceof SyntaxError)
        ? highlightError(ast.value, query.value)
        : highlight(ast.value, query.value, ast.value)
    } catch (_) {
      return query.value
    }
  })

  return {
    ast,
    highlighted,
    isValid: computed(() => !(ast.value instanceof SyntaxError) && ast.value !== null),
  }
}
