import sys
import importlib
import json
import os
import inspect
import mcdc_helper
import mcdc_calculator

# 辅助函数：将调试信息打印到标准错误流
def debug_print(message):
    print(f"[DEBUG]{message}", file=sys.stderr)

# 辅助函数：将测试输出打印到标准错误流
def test_output_print(message):
    print(message, file=sys.stderr)

# 动态加载测试模块并运行其测试
def run_test_module(module_path):
    try:
        test_module = importlib.import_module(module_path)
        debug_print(f" Successfully imported module: {module_path}")

        if hasattr(test_module, 'run_tests') and inspect.isfunction(test_module.run_tests):
            debug_print(" Calling run_tests() function from the imported test module.")
            test_output_print("[TEST OUTPUT BEGIN]") # 用于标识测试输出块的开始
            test_module.run_tests()
            test_output_print("[TEST OUTPUT END]")   # 用于标识测试输出块的结束
        else:
            print(f"[ERROR] Test module {module_path} does not contain a 'run_tests' function.", file=sys.stderr)
            sys.exit(1)

    except ModuleNotFoundError as e:
        print(f"[ERROR] Error during test execution: {e}", file=sys.stderr)
        sys.exit(1)
    except Exception as e:
        print(f"[ERROR] An unexpected error occurred during test execution: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc(file=sys.stderr) # 打印堆栈跟踪到标准错误流
        sys.exit(1)

def generate_coverage_report(all_traces_by_expression):
    total_test_cases = 0
    coverage_map = {}
    covered_sum = 0
    total_sum = 0

    all_flat_traces = []

    for expression_tag, traces_list in all_traces_by_expression.items():
        mcdc_detail = mcdc_calculator.compute_mcdc_detail(traces_list)

        coverage_map[expression_tag] = mcdc_detail['ratio']
        covered_sum += mcdc_detail['coveredCount']
        total_sum += mcdc_detail['totalCount']

        total_test_cases = max(total_test_cases, len(traces_list))

        all_flat_traces.extend(traces_list)

    serializable_traces = []
    for trace_obj in all_flat_traces:
        serializable_traces.append({
            "condValues": trace_obj.getCondValues(),
            "decisionValue": trace_obj.getDecisionValue()
        })

    return {
        "totalTestCases": total_test_cases,
        "coverageMap": coverage_map,
        "coveredSum": covered_sum,
        "totalSum": total_sum,
        "traces": serializable_traces
    }

def main():
    if len(sys.argv) < 3:
        print("Usage: python mcdc_runner.py <work_dir> <test_file_name>", file=sys.stderr)
        sys.exit(1)

    work_dir = sys.argv[1]
    test_file_name = sys.argv[2]

    sys.path.insert(0, work_dir)
    sys.path.insert(0, os.path.join(work_dir, "python_src"))
    sys.path.insert(0, os.path.join(work_dir, "python_test"))

    debug_print(f" sys.path after modifications: {sys.path}")

    module_path = test_file_name.replace(".py", "").replace("/", ".")

    try:
        run_test_module(module_path)

        mcdc_trace_data = mcdc_helper.get_all_traces()
        debug_print(f" Collected {len(mcdc_trace_data)} MCDC trace entries.")

        coverage_report = generate_coverage_report(mcdc_trace_data)

        # 仅将 JSON 输出打印到标准输出流 (sys.stdout)
        print(json.dumps(coverage_report))

    except Exception as e:
        print(f"[ERROR] An unexpected error occurred: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc(file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()