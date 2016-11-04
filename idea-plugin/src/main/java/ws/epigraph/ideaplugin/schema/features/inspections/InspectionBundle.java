package ws.epigraph.ideaplugin.schema.features.inspections;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:konstantin.sobolev@gmail.com">Konstantin Sobolev</a>
 */
public class InspectionBundle {
  private static Reference<ResourceBundle> bundleRef;

  @NonNls
  private static final String BUNDLE = "ws.epigraph.ideaplugin.schema.features.inspections.InspectionBundle";

  public static String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
    return CommonBundle.message(getBundle(), key, params);
  }

  private static ResourceBundle getBundle() {
    ResourceBundle bundle = null;

    if (bundleRef != null) bundle = bundleRef.get();

    if (bundle == null) {
      bundle = ResourceBundle.getBundle(BUNDLE);
      bundleRef = new SoftReference<>(bundle);
    }

    return bundle;
  }
}
