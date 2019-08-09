package com.adapter.bpmn.bpmnjs.exclusivegateway;

import com.adapter.bpmn.bpmnjs.*;
import com.adapter.bpmn.model.connectingobject.ConditionalFlow;
import com.adapter.bpmn.model.flowobject.FlowObject;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.adapter.bpmn.bpmnjs.adapter.AdapterHelper.*;

public class ExclusiveGatewayBPMNElementAdapter implements BPMNElementAdapter {


    private String name;
    private List<ConditionalFlow> conditionalFlows;

    public ExclusiveGatewayBPMNElementAdapter(String name, List<ConditionalFlow> conditionalFlows) {

        this.name = name;
        this.conditionalFlows = conditionalFlows;
    }

    @Override
    public BPMNDiagramElement addElement(BPMNJsDiagram bpmnJsDiagram, String conditionalFlowName) {
        String nextId = bpmnJsDiagram.getNextId();
        Position currentPosition = bpmnJsDiagram.getCurrentPosition();
        Map<Class<? extends FlowObject>, BPMNElementAdapterFactory> dictionary = bpmnJsDiagram.getDictionary();

        Element exclusiveGateway = new Element(nextId, name, new ExclusiveGatewayShapeBound(currentPosition));

        BPMNDiagramElement exclusiveGatewayDiagram = createElementWithCustomLabelAndSequenceFlow(ExclusiveGateway.class, exclusiveGateway, new ExclusiveGatewayLabel(currentPosition), new ExclusiveGatewayFlowPoints(currentPosition), bpmnJsDiagram);
        bpmnJsDiagram.setLastNode(exclusiveGatewayDiagram);
        bpmnJsDiagram.incrementX();

        List<BPMNDiagramElement> conditionalFlowsLastElement = new ArrayList<>();

        int size = conditionalFlows.size();
        for(int i =0;i<size;i++){
            ConditionalFlow conditionalFlow = conditionalFlows.get(i);
            conditionalFlowName = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(conditionalFlow.getExpression().getClass().getSimpleName()), ' ');
            for (FlowObject flowObject : conditionalFlow.getFlowObjects()) {
                BPMNElementAdapter adapter = dictionary.get(flowObject.getClass()).getAdapter(flowObject);
                bpmnJsDiagram.setLastNode(adapter.addElement(bpmnJsDiagram, conditionalFlowName));
                conditionalFlowName = "";
            }
            if(i<size-1) {
                for (FlowObject flowObject : conditionalFlow.getFlowObjects()) {
                    bpmnJsDiagram.decrementX();
                }
            }
            conditionalFlowsLastElement.add(bpmnJsDiagram.getLastNode());
            bpmnJsDiagram.setLastNode(exclusiveGatewayDiagram);
            bpmnJsDiagram.incrementY();
        }

        for (ConditionalFlow conditionalFlow : conditionalFlows) {
            bpmnJsDiagram.decrementY();
        }


        if (conditionalFlowsLastElement.stream().filter((element) -> !(element.getFlowNode() instanceof EndEvent)).count() > 0) {
            BPMNDiagramElement conditionalFlowLastElement = conditionalFlowsLastElement.get(0);
            bpmnJsDiagram.setLastNode(conditionalFlowLastElement);
            String balanceId = nextId + "_balance";

            Element balanceExclusiveGateway = new Element(balanceId, "", new ExclusiveGatewayShapeBound(currentPosition));

            BPMNDiagramElement balanceExclusiveGatewayElement = createElementWithSequenceFlow(ExclusiveGateway.class,balanceExclusiveGateway, new ExclusiveGatewayFlowPoints(currentPosition), null, bpmnJsDiagram);
            bpmnJsDiagram.setLastNode(balanceExclusiveGatewayElement);

            conditionalFlowsLastElement.stream().skip(1).forEach((element) -> {
                createSequenceFlow(element, balanceExclusiveGatewayElement, "", bpmnJsDiagram.getProcess(), bpmnJsDiagram.getPlane(), bpmnJsDiagram.getModelInstance());
            });
        }
        currentPosition.incrementX();
        return bpmnJsDiagram.getLastNode();
    }


}
