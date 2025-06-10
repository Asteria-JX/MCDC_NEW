# python_mcdc/mcdc_helper.py
import json
import sys # 导入 sys 模块

# 全局字典，用于存储所有决策跟踪数据
all_traces = {}

class DecisionTrace:
    def __init__(self, cond_values, decision_value):
        self.cond_values = cond_values
        self.decision_value = decision_value

    def getCondValues(self):
        return self.cond_values

    def getDecisionValue(self):
        return self.decision_value

    def __repr__(self):
        return f"DecisionTrace(cond_values={self.cond_values}, decision_value={self.decision_value})"


def record_trace(class_name, line_tag, cond_values, decision_value):
    expression_tag = f"{class_name}#{line_tag}"
    trace = DecisionTrace(cond_values, decision_value)

    if expression_tag not in all_traces:
        all_traces[expression_tag] = []
    all_traces[expression_tag].append(trace)

    # 将调试信息打印到标准错误流
    print(f"[DEBUG][mcdc_helper.record_trace] Recording trace for {expression_tag}: conds={cond_values}, dec={decision_value}", file=sys.stderr)


def get_all_traces():
    # 将调试信息打印到标准错误流
    print(f"[DEBUG][mcdc_helper.get_all_traces] Retrieved {len(all_traces)} entries.", file=sys.stderr)
    return all_traces


def clear_all_traces():
    # 将调试信息打印到标准错误流
    print(f"[DEBUG][mcdc_helper.clear_all_traces] Clearing {len(all_traces)} recorded traces.", file=sys.stderr)
    all_traces.clear()