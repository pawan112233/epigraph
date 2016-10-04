package io.epigraph.idl;

import com.intellij.psi.util.PsiTreeUtil;
import io.epigraph.idl.operations.Operation;
import io.epigraph.idl.operations.OperationsPsiParser;
import io.epigraph.idl.parser.psi.*;
import io.epigraph.lang.Fqn;
import io.epigraph.psi.EpigraphPsiUtil;
import io.epigraph.psi.PsiProcessingException;
import io.epigraph.refs.TypesResolver;
import io.epigraph.refs.ValueTypeRef;
import io.epigraph.types.DataType;
import io.epigraph.types.DatumType;
import io.epigraph.types.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class IdlPsiParser {
  @NotNull
  public static Idl parseIdl(@NotNull IdlFile idlPsi, @NotNull TypesResolver resolver) throws PsiProcessingException {

    @Nullable IdlNamespaceDecl namespaceDeclPsi = PsiTreeUtil.getChildOfType(idlPsi, IdlNamespaceDecl.class);
    if (namespaceDeclPsi == null)
      throw new PsiProcessingException("namespace not specified", idlPsi);
    @Nullable IdlFqn namespaceFqnPsi = namespaceDeclPsi.getFqn();
    if (namespaceFqnPsi == null)
      throw new PsiProcessingException("namespace not specified", idlPsi);

    Fqn namespace = namespaceFqnPsi.getFqn();

    // todo parse imports

    @Nullable IdlResourceDef[] resourceDefsPsi = PsiTreeUtil.getChildrenOfType(idlPsi, IdlResourceDef.class);

    final List<Resource> resources;

    if (resourceDefsPsi != null) {
      resources = new ArrayList<>(resourceDefsPsi.length);
      for (IdlResourceDef resourceDefPsi : resourceDefsPsi) {
        if (resourceDefPsi != null) {
          resources.add(parseResource(resourceDefPsi, resolver));
        }
      }
    } else
      resources = Collections.emptyList();


    return new Idl(namespace, resources);
  }

  public static Resource parseResource(@NotNull IdlResourceDef psi, @NotNull TypesResolver resolver)
      throws PsiProcessingException {
    final String fieldName = psi.getQid().getCanonicalName();

    @NotNull IdlResourceType resourceTypePsi = psi.getResourceType();

    // todo take imports into account
    @NotNull IdlValueTypeRef valueTypeRefPsi = resourceTypePsi.getValueTypeRef();
    @NotNull ValueTypeRef valueTypeRef = TypeRefs.fromPsi(valueTypeRefPsi);
    @Nullable DataType dataType = resolver.resolve(valueTypeRef);

    if (dataType == null) throw new PsiProcessingException(
        String.format("Can't resolve resource '%s' type '%s'", fieldName, valueTypeRef),
        resourceTypePsi
    );

    // convert datum type to samovar
    @NotNull Type type = dataType.type;
    if (dataType.defaultTag == null && valueTypeRef.defaultOverride() == null && type instanceof DatumType) {
      dataType = new DataType(dataType.polymorphic, type, ((DatumType) type).self);
    }

    @NotNull List<IdlOperationDef> defsPsi = psi.getOperationDefList();

    final List<Operation> operations = new ArrayList<>(defsPsi.size());
    for (IdlOperationDef defPsi : defsPsi)
      operations.add(OperationsPsiParser.parseOperation(dataType, defPsi, resolver));

    return new Resource(
        fieldName, dataType, operations, EpigraphPsiUtil.getLocation(psi)
    );
  }
}