# python_mcdc/mcdc_calculator.py

# 这个文件负责根据MCDC的原理计算给定决策点的覆盖率详细信息。

def array_to_key(arr):
    """将布尔数组转换为字符串键（例如：[True, False] -> '10'）。"""
    return "".join(['1' if b else '0' for b in arr])

def key_to_array(key):
    """将字符串键转换回布尔数组（例如：'10' -> [True, False]）。"""
    return [c == '1' for c in key]

def compute_mcdc_detail(traces):
    """
    计算给定决策跟踪列表的 MCDC 覆盖率详情。

    参数:
        traces (list): MCDCHelper.DecisionTrace 对象的列表，代表一个决策点在多次执行中的跟踪。

    返回:
        dict: 包含 'coveredCount', 'totalCount', 'ratio' 的字典。
    """
    if not traces:
        return {"coveredCount": 0, "totalCount": 0, "ratio": 0.0}

    # trace_map 存储每个条件下子条件值的组合及其对应的最终决策值
    # 例如：{'10': True, '01': False}
    trace_map = {}
    n_conds = 0

    # 确保 traces 不为空，并获取条件数量
    if traces:
        # 假设所有 DecisionTrace 对象的 cond_values 长度相同
        n_conds = len(traces[0].getCondValues())
        for dt in traces: # dt 是 MCDCHelper.DecisionTrace 对象
            key = array_to_key(dt.getCondValues())
            trace_map[key] = dt.getDecisionValue()
    else:
        return {"coveredCount": 0, "totalCount": 0, "ratio": 0.0} # 理论上这行不会执行，但为了安全

    covered = 0
    total = 0

    # 遍历 trace_map 中的每个条目，寻找 MCDC 对
    for key, dec_val in trace_map.items():
        conds = key_to_array(key)

        for i in range(n_conds):
            # 翻转当前条件的值
            flipped = list(conds) # 转换为列表以便修改
            flipped[i] = not flipped[i]
            flipped_key = array_to_key(flipped)
            total += 1 # 每次尝试翻转都计入总数

            # 查找翻转后的条件组合是否存在于 trace_map 中
            other_dec = trace_map.get(flipped_key)
            if other_dec is not None and other_dec != dec_val:
                # 找到 MCDC 对（当前条件翻转，且最终决策值也翻转）
                covered += 1

    # 每个 MCDC 对会找到两次（例如，A->B 和 B->A），所以需要除以2
    covered //= 2
    total //= 2

    ratio = covered / total if total > 0 else 0.0

    return {
        "coveredCount": covered,
        "totalCount": total,
        "ratio": ratio
    }