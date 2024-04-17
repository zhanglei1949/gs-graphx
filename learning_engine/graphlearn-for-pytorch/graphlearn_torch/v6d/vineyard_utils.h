/* Copyright 2022 Alibaba Group Holding Limited. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

#include <torch/extension.h>
namespace graphlearn_torch {
namespace vineyard_utils {

std::tuple<torch::Tensor, torch::Tensor, torch::Tensor> ToCSR(
  const std::string& ipc_socket, const std::string& object_id_str,
  const std::string& v_label_name, const std::string& e_label_id_name,
  const std::string& edge_dir, bool has_eid);

torch::Tensor LoadVertexFeatures(
  const std::string& ipc_socket, const std::string& object_id_str,
  const std::string& v_label_name, std::vector<std::string>& vcols
);

torch::Tensor LoadEdgeFeatures(
  const std::string& ipc_socket, const std::string& object_id_str,
  const std::string& e_label_name, std::vector<std::string>& ecols
);

} // namespace vineyard_utils
}  // namespace graphlearn_torch
