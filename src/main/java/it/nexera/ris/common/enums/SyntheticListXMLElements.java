package it.nexera.ris.common.enums;

/**
 * (non-Javadoc)
 *
 * @author Vlad Strunenko
 * @see it.nexera.ris.common.enums.PropertyXMLElements
 */
public enum SyntheticListXMLElements implements XMLElements {

    INSPECTION_DATE("ElencoSintetico.DataRichiesta", false),
    PROVINCIAL_OFFICE("", false),
    RECLAME_PROPERTY_SERVICE("ElencoSintetico.DescrizioneUfficio", false),
    COMPUTER_UPDATE_FROM("SituazioneAggiornamento.DataDal", true),
    COMPUTER_UPDATE_TO("SituazioneAggiornamento.DataAl", true),
    PERIOD_RETRIEV_VALIDAT_FROM("SituazioneAggiornamento.DataDal", true),
    PERIOD_RETRIEV_VALIDAT_TO("SituazioneAggiornamento.DataAl", true);

    private String element;

    private boolean specialFlow;

    private SyntheticListXMLElements(String element, boolean specialFlow) {
        this.element = element;
        this.specialFlow = specialFlow;
    }

    public String getElement() {
        return element;
    }

    public boolean isSpecialFlow() {
        return specialFlow;
    }

}
