package io.prediction

trait AbstractEvaluator {

  def getParamsSetBase(params: BaseEvaluationParams):
    Seq[(BaseTrainingDataParams, BaseEvaluationDataParams)]

  def evaluateBase(
    feature: BaseFeature,
    predicted: BaseTarget,
    actual: BaseTarget): BaseEvaluationUnit

  def reportBase(evalUnits: Seq[BaseEvaluationUnit]): BaseEvaluationResults

}

trait BaseEvaluator[
    -EP,
    +TDP <: BaseTrainingDataParams,
    +EDP <: BaseEvaluationDataParams,
    -F,
    -T,
    EU <: BaseEvaluationUnit,
    ER <: BaseEvaluationResults
    ]
  extends AbstractEvaluator {

  override def getParamsSetBase(params: BaseEvaluationParams): Seq[(TDP, EDP)] =
    getParamsSet(params.asInstanceOf[EP])

  def getParamsSet(params: EP): Seq[(TDP, EDP)]

  override def evaluateBase(
    feature: BaseFeature,
    predicted: BaseTarget,
    actual: BaseTarget): BaseEvaluationUnit =
    evaluate(
      feature.asInstanceOf[F],
      predicted.asInstanceOf[T],
      actual.asInstanceOf[T])

  def evaluate(feature: F, predicted: T, actual: T): EU

  //override 
  def reportBase(evalUnits: Seq[BaseEvaluationUnit]): BaseEvaluationResults = {
    report(evalUnits.map(_.asInstanceOf[EU]))
  }

  def report(evalUnits: Seq[EU]): ER

}

trait AbstractDataPreparator {

  def prepareTrainingBase(params: BaseTrainingDataParams): BaseTrainingData

}

trait BaseDataPreparator[-TDP, +TD <: BaseTrainingData]
  extends AbstractDataPreparator {

  override def prepareTrainingBase(params: BaseTrainingDataParams): TD =
    prepareTraining(params.asInstanceOf[TDP])

  def prepareTraining(params: TDP): TD

}

trait AbstractEvaluationPreparator {

  def prepareEvaluationBase(params: BaseEvaluationDataParams):
    Seq[(BaseFeature, BaseTarget)]

}

trait BaseEvaluationPreparator[-EDP, +F <: BaseFeature, +T <: BaseTarget]
  extends AbstractEvaluationPreparator {

  override def prepareEvaluationBase(params: BaseEvaluationDataParams):
    Seq[(F, T)] = prepareEvaluation(params.asInstanceOf[EDP])

  def prepareEvaluation(params: EDP): Seq[(F, T)]

}

trait AbstractAlgorithm {

  def initBase(baseAlgoParams: BaseAlgoParams): Unit

  def trainBase(trainingData: BaseTrainingData): BaseModel

  def predictBase(baseModel: BaseModel, feature: BaseFeature): BaseTarget

}

trait BaseAlgorithm[
    -TD,
    -F,
    +T <: BaseTarget,
    M <: BaseModel,
    AP <: BaseAlgoParams]
  extends AbstractAlgorithm {

  override def initBase(baseAlgoParams: BaseAlgoParams): Unit =
    init(baseAlgoParams.asInstanceOf[AP])

  def init(algoParams: AP): Unit = {}

  override def trainBase(trainingData: BaseTrainingData): BaseModel =
    train(trainingData.asInstanceOf[TD])

  def train(trainingData: TD): M

  override def predictBase(
    baseModel: BaseModel,
    feature: BaseFeature): BaseTarget =
    predict(baseModel.asInstanceOf[M], feature.asInstanceOf[F])

  def predict(model: M, feature: F): T

}

trait AbstractServer {

  def initBase(baseServerParams: BaseServerParams): Unit

  def combineBase(feature: BaseFeature, targets: Seq[BaseTarget]): BaseTarget

}

trait BaseServer[-F, T <: BaseTarget, SP <: BaseServerParams]
    extends AbstractServer {

  override def initBase(baseServerParams: BaseServerParams): Unit =
    init(baseServerParams.asInstanceOf[SP])

  def init(serverParams: SP): Unit = {}

  override def combineBase(feature: BaseFeature, targets: Seq[BaseTarget]) =
    combine(feature.asInstanceOf[F], targets.map(_.asInstanceOf[T]))

  def combine(feature: F, targets: Seq[T]): T

}

class AbstractEngine(

  val dataPreparatorClass: Class[_ <: AbstractDataPreparator],

  val algorithmClassMap: Map[String, Class[_ <: AbstractAlgorithm]],

  val serverClass: Class[_ <: AbstractServer]) {

}

class BaseEngine[
    TDP <: BaseTrainingDataParams,
    TD <: BaseTrainingData, 
    F <: BaseFeature, 
    T <: BaseTarget](
    dataPreparatorClass: Class[_ <: BaseDataPreparator[TDP, TD]],
    algorithmClassMap: 
      Map[String, 
        Class[_ <: 
          BaseAlgorithm[TD, F, T, _ <: BaseModel, _ <: BaseAlgoParams]]],
    serverClass: Class[_ <: BaseServer[F, T, _ <: BaseServerParams]])
  extends AbstractEngine(dataPreparatorClass, algorithmClassMap, serverClass) {
}