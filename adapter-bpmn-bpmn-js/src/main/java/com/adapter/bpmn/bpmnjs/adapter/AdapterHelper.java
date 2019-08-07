package com.adapter.bpmn.bpmnjs.adapter;

import com.adapter.bpmn.bpmnjs.BPMNDiagram;
import com.adapter.bpmn.bpmnjs.BPMNDiagramElement;
import com.adapter.bpmn.bpmnjs.Element;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnEdge;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnLabel;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape;
import org.camunda.bpm.model.bpmn.instance.dc.Bounds;
import org.camunda.bpm.model.bpmn.instance.di.Waypoint;

public class AdapterHelper {

    static <T extends BpmnModelElementInstance> BPMNDiagramElement createElementWithSequenceFlow(Class<T> type, BPMNDiagram bpmnDiagram, Element element) {
        FlowNode flowNode = (FlowNode) createElement(type, bpmnDiagram, element);
        return getBpmnDiagramElement(bpmnDiagram, element, flowNode, bpmnDiagram.getPlane());
    }

    static <T extends BpmnModelElementInstance> BPMNDiagramElement createElementWithCustomLabelAndSequenceFlow(Class<T> type, BPMNDiagram bpmnDiagram, Element element) {
        FlowNode flowNode = (FlowNode) createElementWitCustomhLabel(type, bpmnDiagram, element);
        return getBpmnDiagramElement(bpmnDiagram, element, flowNode, bpmnDiagram.getPlane());
    }

    static <T extends BpmnModelElementInstance> T createElementWitCustomhLabel(Class<T> type, BPMNDiagram bpmnDiagram, Element element) {
        BpmnModelInstance modelInstance = bpmnDiagram.getModelInstance();
        String id = element.getId();
        BpmnLabel label = getLabel(modelInstance, id + "_label", element.getLabelXPosition(), element.getLabelYPosition());
        return createElement(type,
                modelInstance,
                bpmnDiagram.getPlane(),
                bpmnDiagram.getParentElement(),
                id,
                element.getName(),
                element.getShapeBoundXPosition(),
                element.getShapeBoundYPosition(),
                element.getShapeBoundHeight(),
                element.getShapeBoundWidth(),
                label);
    }


    static <T extends BpmnModelElementInstance> T createElement(Class<T> type, BPMNDiagram bpmnDiagram, Element element) {
        return createElement(type,
                bpmnDiagram.getModelInstance(),
                bpmnDiagram.getPlane(),
                bpmnDiagram.getParentElement(),
                element.getId(),
                element.getName(),
                element.getShapeBoundXPosition(),
                element.getShapeBoundYPosition(),
                element.getShapeBoundHeight(),
                element.getShapeBoundWidth(),
                null);
    }

    private static BPMNDiagramElement getBpmnDiagramElement(BPMNDiagram bpmnDiagram, Element element, FlowNode flowNode, BpmnPlane plane) {
        BPMNDiagramElement bpmnDiagramElement = new BPMNDiagramElement(flowNode, element.getLeftFlowPoint(), element.getRightFlowPoint(), element.getDownFlowPoint());

        BpmnEdge bpmnEdge = createSequenceFlow(bpmnDiagram.getModelInstance(), bpmnDiagram.getParentElement(), bpmnDiagram.getCurrentElement(), bpmnDiagramElement, element.getConditionalFlowName(), plane);



        return bpmnDiagramElement;
    }

    static BpmnEdge createSequenceFlow(BpmnModelInstance modelInstance, Process process, BPMNDiagramElement from, BPMNDiagramElement to, String conditionalFlowName, BpmnPlane plane) {
        FlowNode fromFlowNode = from.getFlowNode();
        FlowNode toFlowNode = to.getFlowNode();
        String identifier = fromFlowNode.getId() + "-" + toFlowNode.getId();
        SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
        sequenceFlow.setAttributeValue("id", identifier, true);
        sequenceFlow.setAttributeValue("name", conditionalFlowName, false);
        process.addChildElement(sequenceFlow);
        sequenceFlow.setSource(fromFlowNode);
        fromFlowNode.getOutgoing().add(sequenceFlow);
        sequenceFlow.setTarget(toFlowNode);
        toFlowNode.getIncoming().add(sequenceFlow);

        BpmnEdge bpmnEdge = modelInstance.newInstance(BpmnEdge.class);
        bpmnEdge.setId("edge_" + identifier);
        bpmnEdge.setBpmnElement(sequenceFlow);

        if(from.getRightFlowPoint().getY() < to.getLeftFlowPoint().getY()){
            Waypoint wp1 = modelInstance.newInstance(Waypoint.class);
            wp1.setX(from.getDownFlowPoint().getX());
            wp1.setY(from.getDownFlowPoint().getY());
            bpmnEdge.addChildElement(wp1);

            Waypoint wp2 = modelInstance.newInstance(Waypoint.class);
            wp2.setX(from.getDownFlowPoint().getX());
            wp2.setY(to.getLeftFlowPoint().getY());
            bpmnEdge.addChildElement(wp2);

            Waypoint wp3 = modelInstance.newInstance(Waypoint.class);
            wp3.setX(to.getLeftFlowPoint().getX());
            wp3.setY(to.getLeftFlowPoint().getY());
            bpmnEdge.addChildElement(wp3);
        } else if(from.getRightFlowPoint().getY() > to.getLeftFlowPoint().getY()){
            Waypoint wp1 = modelInstance.newInstance(Waypoint.class);
            wp1.setX(from.getRightFlowPoint().getX());
            wp1.setY(from.getRightFlowPoint().getY());
            bpmnEdge.addChildElement(wp1);

            Waypoint wp2 = modelInstance.newInstance(Waypoint.class);
            wp2.setX(to.getDownFlowPoint().getX());
            wp2.setY(from.getRightFlowPoint().getY());
            bpmnEdge.addChildElement(wp2);

            Waypoint wp3 = modelInstance.newInstance(Waypoint.class);
            wp3.setX(to.getDownFlowPoint().getX());
            wp3.setY(to.getDownFlowPoint().getY());
            bpmnEdge.addChildElement(wp3);
        } else{

            Waypoint wp1 = modelInstance.newInstance(Waypoint.class);
            wp1.setX(from.getRightFlowPoint().getX());
            wp1.setY(from.getRightFlowPoint().getY());
            bpmnEdge.addChildElement(wp1);

            Waypoint wp2 = modelInstance.newInstance(Waypoint.class);
            wp2.setX(to.getLeftFlowPoint().getX());
            wp2.setY(to.getLeftFlowPoint().getY());
            bpmnEdge.addChildElement(wp2);
        }
        plane.addChildElement(bpmnEdge);

        return bpmnEdge;
    }


    private static <T extends BpmnModelElementInstance> T createElement(Class<T> type,
                                                                        BpmnModelInstance modelInstance,
                                                                        BpmnPlane plane,
                                                                        Process parentElement,
                                                                        String nextId,
                                                                        String name,
                                                                        int shapeBoundXPosition,
                                                                        int shapeBoundYPosition,
                                                                        int shapeBoundHeight,
                                                                        int shapeBoundWidth,
                                                                        BpmnLabel label) {
        T element = modelInstance.newInstance(type);
        element.setAttributeValue("id", nextId, true);
        element.setAttributeValue("name", name, false);
        parentElement.addChildElement(element);

        BpmnShape bpmnShape = modelInstance.newInstance(BpmnShape.class);
        bpmnShape.setBpmnElement((BaseElement) element);
        bpmnShape.setId(nextId + "_shape");
        bpmnShape.setBounds(getBounds(modelInstance, shapeBoundXPosition, shapeBoundYPosition, shapeBoundHeight, shapeBoundWidth));
        if (label != null) {
            bpmnShape.addChildElement(label);
        }

        plane.addChildElement(bpmnShape);
        return element;
    }

    private static Bounds getBounds(BpmnModelInstance modelInstance, int currentXPosition, int currentYPosition, int height, int width) {
        Bounds bounds = modelInstance.newInstance(Bounds.class);
        bounds.setX(currentXPosition);
        bounds.setY(currentYPosition);
        bounds.setHeight(height);
        bounds.setWidth(width);
        return bounds;
    }

    private static BpmnLabel getLabel(BpmnModelInstance modelInstance, String id, int x, int y) {
        BpmnLabel bpmnLabel = modelInstance.newInstance(BpmnLabel.class);
        bpmnLabel.setId(id);
        Bounds labelBounds = getBounds(modelInstance, x, y, 0, 0);
        bpmnLabel.addChildElement(labelBounds);
        return bpmnLabel;
    }
}
