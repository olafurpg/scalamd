package fox

import com.rklaehn.radixtree._
import com.rklaehn.radixtree.RadixTreeInternal._
import org.langmeta.semanticdb.Symbol
import org.langmeta.semanticdb.Signature

package object code {
  type SymbolKey = Array[Signature]
  type SymbolTable = RadixTree[SymbolKey, SymbolData]
  implicit class XtensionSymbolDataTableMembers(val data: SymbolData)
      extends AnyVal {
    def publicMembers(implicit index: Index): List[SymbolData] =
      data.symbol.members(!_.denotation.isPrivate)
    def members(implicit index: Index): List[SymbolData] =
      data.symbol.members(_ => true)
    def members(filter: SymbolData => Boolean)(
        implicit index: Index
    ): List[SymbolData] = data.symbol.members(filter)

    def packageObject(implicit index: Index): Option[SymbolData] = {
      index.table.get(
        Symbol.Global(data.symbol, Signature.Term("package")).toKey
      )
    }
  }
  implicit class XtensionSymbolTableMembers(val symbol: Symbol) extends AnyVal {
    def data(implicit index: Index): Option[SymbolData] =
      index.table.get(symbol.toKey)
    def members(
        filter: SymbolData => Boolean
    )(implicit index: Index): List[SymbolData] = {
      val buf = List.newBuilder[SymbolData]
      index.table.filterPrefix(symbol.toKey).internalChildren.foreach { rt =>
        rt.internalValue.foreach { value =>
          // TODO(olafur) why is this guard necessary? .internalChildren should
          // only return direct children.
          if (value.symbol.owner == symbol && filter(value)) {
            buf += value
          }
        }
      }
      buf.result()
    }
  }
}
