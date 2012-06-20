package jp.sf.amateras.mirage.scala

import java.util.concurrent.atomic.AtomicBoolean
import jp.sf.amateras.mirage.bean.BeanDescFactory

/**
 * Initializes BeanDescFactory for mirage-scala.
 */
object BeanDescFactoryInitializer {

  private var initialized: Boolean = false

  /**
   * Sets ScalaPropertyExtractor to BeanDescFactory.
   */
  def initialize() {
    if(initialized == false){
      BeanDescFactory.setPropertyExtractor(new ScalaPropertyExtractor)
      initialized = true
    }
  }

}