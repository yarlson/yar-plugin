# Yar Language Model

The plugin's grammar defines the following Yar language constructs. This is the language model as understood by the plugin's parser.

## File Structure

- A Yar file starts with a `package` declaration, followed by zero or more `import` declarations, followed by top-level declarations.
- Top-level declarations: `struct`, `interface`, `enum`, `fn` (function). All may have an optional `pub` visibility modifier.

## Type System

- Primitive/builtin types: `bool`, `i32`, `i64`, `str`, `void`, `noreturn`
- Composite types: structs (with fields), interfaces (with methods), enums (with cases, optionally carrying fields)
- Type constructors: pointers (`*T`), arrays (`[N]T`), slices (`[]T`), maps (`map[K]V`), function types (`fn(T) R`), errorable types (`!T`)
- Generic type parameters via bracket syntax: `Name[T]`

## Expressions

- Precedence from lowest to highest: handle (`or |err| { ... }`), logical or, logical and, equality, comparison, additive, multiplicative, unary, postfix
- Postfix operations: function call, dot access, index/slice, error propagation (`?`), struct literal body
- Primary expressions: grouping, function literals, array/slice/map literals, error literals, bool/nil/int/string literals, identifiers

## Statements

- Variable declarations: `var name Type = expr` and short declarations `name := expr`
- Control flow: `if`/`else`, `for` (with optional C-style clause or condition), `break`, `continue`, `return`, `match`/`case`
- Assignment and expression statements

## Functions

- Functions support receivers (method syntax): `fn (self Type) name(params) ReturnType { ... }`
- Functions can be errorable: `fn name(params) !ReturnType { ... }`
- Function literals (closures): `fn(params) ReturnType { ... }`

## Error Handling

- Errorable return types marked with `!`
- Error propagation with `?` postfix operator
- Error handling with `or |err| { ... }` suffix
- Error literal expressions: `error.Name`

## Builtin Functions

The completion provider offers these builtins: `print`, `print_int`, `panic`, `len`, `append`, `has`, `delete`, `keys`.

## Standard Library Packages

Known to completion: `strings`, `utf8`, `conv`, `sort`, `path`, `fs`, `process`, `env`, `stdio`.
